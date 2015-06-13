package org.cognoseed.tweetlazo;

import akka.actor.ActorRef;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 * Listens to tweet streams, sends them into the actor system.
 */
public class TweetListener implements StatusListener {

    private ActorRef dispatcher;

    public TweetListener(ActorRef dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void onStatus(Status status) {
        dispatcher.tell(status, ActorRef.noSender());
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    public void onTrackLimitationNotice(int i) {

    }

    public void onScrubGeo(long l, long l1) {

    }

    public void onStallWarning(StallWarning stallWarning) {
        dispatcher.tell(stallWarning, ActorRef.noSender());
    }

    public void onException(Exception e) {
        dispatcher.tell(e, ActorRef.noSender());
    }
}
