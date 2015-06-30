package org.cognoseed.tweetlazo

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

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())
  val charts = parameters.unnamed.map(new TweetChart(_))
  val cancellables = charts.map(setupChart)
  val stream = new TwitterStreamFactory().getInstance
  stream.addListener(new TweetListener(dispatcher))
  stream.filter(new FilterQuery(0, Array[Long](), parameters.unnamed.toArray))

  stage = new PrimaryStage {
    scene = new Scene {
      root = new VBox {
        children = charts
      }
    }
  }

  override def stopApp() = {
    cancellables.foreach(_.cancel())
    stream.shutdown()
    system.shutdown()
  }

  def setupChart(chart: TweetChart) = {
    dispatcher ! WatchHashtag(chart.hashtag)

    system.scheduler.schedule(1.minute, 1.minute) {
      dispatcher.ask(CountsSinceLast(chart.hashtag))(5.seconds).onSuccess {
        case reply: CountsSinceLastReply => chart.update(reply)
      }
    }
  }

}
