package org.cognoseed.tweetlazo

import akka.actor.ActorSystem
import org.cognoseed.tweetlazo.TweetDispatcher.WatchHashtag
import twitter4j.{FilterQuery, TwitterStreamFactory}

object Main extends App {

  val system = ActorSystem.create("tweetlazo")
  val dispatcher = system.actorOf(TweetDispatcher.props())
  args.foreach(dispatcher ! WatchHashtag(_))

  val stream = new TwitterStreamFactory().getInstance
  stream.addListener(new TweetListener(dispatcher))
  stream.filter(new FilterQuery(0, Array[Long](), args))

  io.StdIn.readLine()
  stream.shutdown()
  system.shutdown()

}
