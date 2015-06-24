package org.cognoseed.tweetlazo

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import akka.event.Logging
import twitter4j.Status

object TweetDispatcher {
  case class WatchHashtag(hashtag: String)
  case class UnwatchHashtag(hashtag: String)

  def props(): Props =
    Props(new TweetDispatcher((factory, key) => factory.actorOf(GoalCounter.props(key), key)))

  def props(maker: (ActorRefFactory, String) => ActorRef): Props = Props(new TweetDispatcher(maker))
}

/** An actor that receives tweets from [[org.cognoseed.tweetlazo.TweetListener]] and distributes the tweets to the
  * correct children actors.
  */
class TweetDispatcher(maker: (ActorRefFactory, String) => ActorRef) extends Actor {
  import GoalCounter._
  import TweetDispatcher._

  val log = Logging(context.system, this)
  var children = Map.empty[String, ActorRef]

  def receive = {
    case tweet: Status =>
      tweet.getHashtagEntities.map(_.getText.toLowerCase).distinct.map(children.get).foreach(_ foreach(_ forward tweet))
    case WatchHashtag(hashtag) =>
      val key = hashtag.toLowerCase
      children = children.updated(key, maker(context, key))
    case UnwatchHashtag(hashtag) =>
      val key = hashtag.toLowerCase
      children.get(key).foreach(context.stop)
      children = children - key
    case msg: Counts => children.get(msg.hashtag.toLowerCase).foreach(_ forward msg)
    case msg: CountsSinceLast => children.get(msg.hashtag.toLowerCase).foreach(_ forward msg)
  }

}
