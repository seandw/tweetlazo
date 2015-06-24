package org.cognoseed.tweetlazo

import java.time._
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.pattern.ask
import twitter4j.{FilterQuery, TwitterStreamFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.chart.XYChart.{Data, Series}
import scalafx.scene.chart.{NumberAxis, StackedAreaChart}
import scalafx.scene.layout.{HBox, Priority}
import scalafx.util.StringConverter

object Main extends JFXApp {
  import org.cognoseed.tweetlazo.GoalCounter._
  import org.cognoseed.tweetlazo.TweetDispatcher._

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())
  val charts = parameters.unnamed.map { hashtag =>
    dispatcher ! WatchHashtag(hashtag)

    val tweetSeries = new Series[Number, Number] {
      name = s"#$hashtag non-goal tweets"
    }

    val goalSeries = new Series[Number, Number] {
      name = s"#$hashtag goal tweets"
    }

    // TODO Replace with a more date-conscious axis
    val dateAxis = new NumberAxis {
      forceZeroInRange = false
      tickLabelFormatter = StringConverter.toStringConverter { time =>
        DateTimeFormatter.ofPattern("HH:mm").format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(time.longValue),
          ZoneId.systemDefault))
      }
    }

    val chart = new StackedAreaChart(dateAxis, new NumberAxis) {
      data = Seq(goalSeries.delegate, tweetSeries.delegate)
      maxWidth = Double.MaxValue
    }
    HBox.setHgrow(chart, Priority.Always)

    val cancellable = system.scheduler.schedule(15.seconds, 15.seconds) {
      dispatcher.ask(CountsSinceLast(hashtag))(5.seconds).onSuccess {
        case reply: CountsSinceLastReply => Platform.runLater {
          val time = Instant.now.getEpochSecond
          goalSeries.getData.add(Data(time, reply.goalTweets))
          tweetSeries.getData.add(Data(time, reply.tweets-reply.goalTweets))
        }
      }
    }

    (chart, cancellable)
  }

  val stream = new TwitterStreamFactory().getInstance
  stream.addListener(new TweetListener(dispatcher))
  stream.filter(new FilterQuery(0, Array[Long](), parameters.unnamed.toArray))

  stage = new PrimaryStage {
    scene = new Scene {
      root = new HBox {
        children = charts.map(_._1)
      }
    }
  }

  override def stopApp() = {
    charts.foreach(_._2.cancel())
    stream.shutdown()
    system.shutdown()
  }

}
