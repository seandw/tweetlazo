package org.cognoseed.tweetlazo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.{Props, Actor}
import akka.event.Logging
import twitter4j.Status

object GoalCounter {
  case class Counts(hashtag: String)
  case class CountsSinceLast(hashtag: String)
  case class CountsReply(hashtag: String, tweets: Long, retweets: Long, goalTweets: Long, goals: Long,
                         extraLetters: Long)
  case class CountsSinceLastReply(hashtag: String, tweets: Long, retweets: Long, goalTweets: Long, goals: Long,
                                  extraLetters: Long)

  lazy val GoalTranslations = Set(
    "goal",
    "gol",
    "goalazo",
    "golazo",
    "mal",
    "tor"
  )

  def props(hashtag: String): Props = Props(new GoalCounter(hashtag))
}

/** Takes in tweets, counts number of tweets, retweets, goals, and extra letters in those goals. */
class GoalCounter(hashtag: String) extends Actor {
  import GoalCounter._
  import Utils._

  val log = Logging(context.system, this)
  val tick = context.system.scheduler.schedule(10.seconds, 10.seconds, self, "tick")

  var tweets = 0L
  var retweets = 0L
  var goalTweets = 0L
  var goals = 0L
  var extraLetters = 0L
  var last = CountsReply(hashtag, tweets, retweets, goalTweets, goals, extraLetters)

  override def postStop() = {
    tick.cancel()
  }

  def receive = {
    case tweet: Status =>
      val words = tweet.getText.trim.toLowerCase.stripPunctuation.stripAccents.split("\\s+")
      val strippedWords = words.map(_.stripDuplicateAdjacents)

      words.zip(strippedWords).foreach {
        case (orig, stripped) if GoalTranslations.contains(stripped) =>
          log.debug(s"TWEET CONTAINS $stripped: $orig")
          goals += 1
          extraLetters += orig.length - stripped.length
        case _ => ()
      }

      tweets += 1
      if (tweet.isRetweet) retweets += 1
      if (strippedWords.exists(GoalTranslations.contains)) goalTweets += 1
    case Counts(_) => sender() ! CountsReply(hashtag, tweets, retweets, goalTweets, goals, extraLetters)
    case CountsSinceLast(_) =>
      sender() ! CountsSinceLastReply(hashtag, tweets-last.tweets, retweets-last.retweets, goalTweets-last.goalTweets,
        goals-last.goals, extraLetters-last.extraLetters)
      last = CountsReply(hashtag, tweets, retweets, goalTweets, goals, extraLetters)
    case "tick" => log.info(s"Counts for #$hashtag - tweets: $tweets, retweets: $retweets, goal tweets: $goalTweets, "
                              + s"goals: $goals, extra letters: $extraLetters")
  }

}
