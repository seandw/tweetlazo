package org.cognoseed.tweetlazo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import org.jfree.data.time.FixedMillisecond
import twitter4j.{FilterQuery, TwitterStreamFactory}

object Main extends App with scalax.chart.module.Charting {
  import org.cognoseed.tweetlazo.GoalCounter._
  import org.cognoseed.tweetlazo.TweetDispatcher._

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())

  val cancellables = args.map { hashtag =>
    dispatcher ! WatchHashtag(hashtag)

    val tweetSeries = s"#$hashtag non-goal tweets"
    val goalSeries = s"#$hashtag goal tweets"

    val collection = new TimeTableXYDataset
    val chart = XYAreaChart.stacked(collection)
    chart.show()

    system.scheduler.schedule(10.seconds, 10.seconds) {
      dispatcher.ask(CountsSinceLast(hashtag))(5.seconds).onSuccess {
        case reply: CountsSinceLastReply => swing.Swing.onEDT {
          val time = new FixedMillisecond
          collection.add(time, reply.goalTweets, goalSeries)
          collection.add(time, reply.tweets-reply.goalTweets, tweetSeries)

        }
      }
    }
  }

  val stream = new TwitterStreamFactory().getInstance
  stream.addListener(new TweetListener(dispatcher))
  stream.filter(new FilterQuery(0, Array[Long](), args))

  io.StdIn.readLine()
  cancellables.foreach(_.cancel())
  stream.shutdown()
  system.shutdown()

}
