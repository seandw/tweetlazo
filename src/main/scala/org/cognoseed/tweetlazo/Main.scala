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
import scalafx.scene.chart.{CategoryAxis, NumberAxis, StackedBarChart}
import scalafx.scene.layout.{Priority, VBox}

object Main extends JFXApp {
  import org.cognoseed.tweetlazo.GoalCounter._
  import org.cognoseed.tweetlazo.TweetDispatcher._

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())
  val charts = parameters.unnamed.map { hashtag =>
    dispatcher ! WatchHashtag(hashtag)

    val tweetSeries = new Series[String, Number] {
      name = s"#$hashtag non-goal tweets"
    }

    val goalSeries = new Series[String, Number] {
      name = s"#$hashtag goal tweets"
    }

    val chart = new StackedBarChart(new CategoryAxis, new NumberAxis) {
      data = Seq(goalSeries.delegate, tweetSeries.delegate)
      categoryGap = 1
      maxHeight = Double.MaxValue
      vgrow = Priority.Always
    }

    val cancellable = system.scheduler.schedule(1.minute, 1.minute) {
      dispatcher.ask(CountsSinceLast(hashtag))(5.seconds).onSuccess {
        case reply: CountsSinceLastReply =>
          val time = DateTimeFormatter.ofPattern("HH:mm").format(ZonedDateTime.now)
          Platform.runLater {
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
      root = new VBox {
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
