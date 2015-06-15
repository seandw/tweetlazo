package org.cognoseed.tweetlazo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Top of the tree of actors, this actor receives tweets from {@link TweetListener} and distributes the tweets to the
 * correct children actors.
 */
public class TweetDispatcher extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    public static Props props(final String[] args) {
        return Props.create(new Creator<TweetDispatcher>() {
            public TweetDispatcher create() throws Exception {
                return new TweetDispatcher(args);
            }
        });
    }

    private String[] hashtags;
    private Map<String, ActorRef> children = new HashMap<>();

    public TweetDispatcher(String[] hashtags) {
        this.hashtags = hashtags;
    }

    @Override
    public void preStart() throws Exception {
        logger.debug("Creating children for each hashtag...");
        for (String hashtag : hashtags) {
            String key = hashtag.substring(1).toLowerCase();
            children.put(key, getContext().actorOf(Props.create(GoalCounter.class, key), key));
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Status) {
            Status status = (Status) message;
            logger.debug("{} : {}", status.getId(), status.getText());
            Set<String> hashtags = new HashSet<>();
            for (HashtagEntity hashtag : status.getHashtagEntities()) {
                hashtags.add(hashtag.getText().toLowerCase());
            }
            for (String key : hashtags) {
                if (children.containsKey(key)) {
                    children.get(key).tell(status, getSelf());
                }
            }
        }
    }
}
