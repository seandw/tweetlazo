package org.cognoseed.tweetlazo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.*;

/**
 * Top of the tree of actors, this actor receives tweets from {@link TweetListener} and distributes the tweets to the
 * correct children actors.
 */
public class TweetDispatcher extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(context().system(), this);

    public static Props props(final String[] args) {
        return Props.create(new Creator<TweetDispatcher>() {
            public TweetDispatcher create() throws Exception {
                return new TweetDispatcher(args);
            }
        });
    }

    String[] hashtags;
    Map<String, ActorRef> children = new HashMap<>();

    public TweetDispatcher(String[] hashtags) {
        this.hashtags = hashtags;
    }

    @Override
    public void preStart() throws Exception {
        logger.debug("Creating children for each hashtag...");
        for (String hashtag : hashtags) {
            String key = hashtag.substring(1).toLowerCase();
            children.put(key, context().actorOf(Props.create(GoalCounter.class, key), key));
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Status) {
            final Status status = (Status) message;
            logger.debug("{} : {}", status.getId(), status.getText());

            // find the hashtags in the tweet that we're tracking, and forward the tweet to them. ONCE.
            Arrays.stream(status.getHashtagEntities())
                    .map(HashtagEntity::getText)
                    .map(String::toLowerCase)
                    .distinct()
                    .filter(children::containsKey)
                    .map(children::get)
                    .forEach(ref -> ref.tell(status, self()));
        } else {
            unhandled(message);
        }
    }
}
