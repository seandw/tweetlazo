package org.cognoseed.tweetlazo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class App {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("Tweetlazo");
        ActorRef dispatcher = system.actorOf(TweetDispatcher.props(args));

        TwitterStream stream = new TwitterStreamFactory().getInstance();
        stream.addListener(new TweetListener(dispatcher));
        stream.filter(new FilterQuery(0, new long[] {}, args));
    }

}
