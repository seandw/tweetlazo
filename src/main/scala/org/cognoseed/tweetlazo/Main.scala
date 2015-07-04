package org.cognoseed.tweetlazo

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime}

import akka.actor.ActorSystem
import akka.pattern.ask
import twitter4j.{FilterQuery, TwitterStreamFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Side
import scalafx.scene.Scene
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control.{Tab, TabPane}

object Main extends JFXApp {
  import org.cognoseed.tweetlazo.GoalCounter._
  import org.cognoseed.tweetlazo.TweetDispatcher._

  // TODO add a feature to end tracking at a specified time or duration
  // prefer a specific start time to an amount of delay
  val start = (parameters.named.get("start"), parameters.named.get("delay")) match {
    case (Some(time), _) => getNextOccurrence(time)
    case (None, Some(duration)) => duration.toLong.minutes
    case (None, None) => 0.seconds
  }
  val end = parameters.named.get("end") match {
    case Some(time) => Option(getNextOccurrence(time))
    case None => Option.empty[FiniteDuration]
  }
  require(start < end.getOrElse(Duration.Inf), "Can't end the stream before it starts!")

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())
  val stream = new TwitterStreamFactory().getInstance
  val streamStarter = system.scheduler.scheduleOnce(start) {
    stream.addListener(new TweetListener(dispatcher))
    stream.filter(new FilterQuery(0, Array[Long](), parameters.unnamed.toArray))
  }
  val streamEnder = end.map(system.scheduler.scheduleOnce(_)(cancelStreamAndWatchers()))
  val charts = parameters.unnamed.map(new TweetChart(_))
  val watchers = charts.map(updateChart)

  stage = new PrimaryStage {
    scene = new Scene {
      root = new TabPane {
        tabs = charts.map(chart => new Tab {
          content = chart
          text = s"#${chart.hashtag}"
        })
        side = Side.RIGHT
        tabClosingPolicy = TabClosingPolicy.UNAVAILABLE
      }
    }
  }

  override def stopApp() = {
    streamStarter.cancel()
    streamEnder.foreach(_.cancel())
    cancelStreamAndWatchers()
    system.shutdown()
  }

  def cancelStreamAndWatchers() = {
    watchers.foreach(_.cancel())
    stream.shutdown()
  }

  def updateChart(chart: TweetChart) = {
    dispatcher ! WatchHashtag(chart.hashtag)

    system.scheduler.schedule(start + 1.minute, 1.minute) {
      dispatcher.ask(CountsSinceLast(chart.hashtag))(5.seconds).onSuccess {
        case reply: CountsSinceLastReply => chart.update(reply)
      }
    }
  }

  def getNextOccurrence(time: String) = {
    val possibleTime = LocalTime.parse(time).atDate(LocalDate.now)
    val actualTime = if (possibleTime.isAfter(LocalDateTime.now)) possibleTime else possibleTime.plusDays(1)
    LocalDateTime.now.until(actualTime, ChronoUnit.SECONDS).seconds
  }

}
