package org.cognoseed.tweetlazo

import java.time.LocalTime
import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import akka.pattern.ask
import twitter4j.{FilterQuery, TwitterStreamFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.VBox

object Main extends JFXApp {
  import org.cognoseed.tweetlazo.GoalCounter._
  import org.cognoseed.tweetlazo.TweetDispatcher._

  // TODO add a feature to end tracking at a specified time or duration
  // prefer a specific start time to an amount of delay
  val delay = (parameters.named.get("start"), parameters.named.get("delay")) match {
    case (Some(time), _) => LocalTime.now.until(LocalTime.parse(time), ChronoUnit.SECONDS).seconds
    case (None, Some(duration)) => duration.toLong.minutes
    case (None, None) => 0.seconds
  }

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())
  val stream = new TwitterStreamFactory().getInstance
  val streamStarter = system.scheduler.scheduleOnce(delay)(startStream())
  val charts = parameters.unnamed.map(new TweetChart(_))
  val cancellables = charts.map(updateChart)

  stage = new PrimaryStage {
    scene = new Scene {
      root = new VBox {
        children = charts
      }
    }
  }

  override def stopApp() = {
    cancellables.foreach(_.cancel())
    streamStarter.cancel()
    stream.shutdown()
    system.shutdown()
  }

  def updateChart(chart: TweetChart) = {
    dispatcher ! WatchHashtag(chart.hashtag)

    system.scheduler.schedule(delay + 1.minute, 1.minute) {
      dispatcher.ask(CountsSinceLast(chart.hashtag))(5.seconds).onSuccess {
        case reply: CountsSinceLastReply => chart.update(reply)
      }
    }
  }

  def startStream() = {
    stream.addListener(new TweetListener(dispatcher))
    stream.filter(new FilterQuery(0, Array[Long](), parameters.unnamed.toArray))
  }

}
