package org.cognoseed.tweetlazo;

import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import twitter4j.Status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Takes in tweets, counts # of tweets, goals, and extra letters in those goals.
 */
public class GoalCounter extends UntypedActor {

    public static class Counts { }
    public static class CountsReply {
        public final long tweets;
        public final long retweets;
        public final long goalTweets;
        public final long goals;
        public final long extraLetters;

        public CountsReply(long tweets, long retweets, long goalTweets, long goals, long extraLetters) {
            this.tweets = tweets;
            this.retweets = retweets;
            this.goalTweets = goalTweets;
            this.goals = goals;
            this.extraLetters = extraLetters;
        }
    }

    private static final Set<String> GOAL_TRANSLATIONS = new HashSet<>(Arrays.asList(
            "goal",
            "gol",
            "goalazo",
            "golazo",
            "mal"
    ));

    private final LoggingAdapter logger = Logging.getLogger(context().system(), this);

    String hashtag;
    long nTweets;
    long nRetweets;
    long nGoalTweets;
    long nGoals;
    long nExtraLetters;
    final Cancellable tick = context().system().scheduler().schedule(
            Duration.create(0, TimeUnit.MILLISECONDS), Duration.create(10, TimeUnit.SECONDS),
            self(), new Counts(), context().dispatcher(), null);

    public GoalCounter(String hashtag) {
        this.hashtag = hashtag;
    }

    @Override
    public void postStop() throws Exception {
        tick.cancel();
        logger.info("Final counts for #" + hashtag + ": tweets - " + nTweets +
                        ", retweets - {}, goal tweets - {}, goals - {}, extra letters - {}",
                nRetweets, nGoalTweets, nGoals, nExtraLetters);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Status) {
            boolean encounteredGoal = false;
            Status status = (Status) message;
            String[] words = StringUtils.stripPunctuation(status.getText()).trim().split("\\s+");
            for (String word : words) {
                String stripped = StringUtils.stripDuplicateAdjacents(word);
                if (GOAL_TRANSLATIONS.contains(stripped)) {
                    logger.debug("TWEET CONTAINS {}: {}", stripped, word);
                    encounteredGoal = true;
                    ++nGoals;
                    nExtraLetters += word.length() - stripped.length();
                }
            }
            ++nTweets;
            if (encounteredGoal) ++nGoalTweets;
            if (status.isRetweet()) ++nRetweets;
        } else if (message instanceof Counts) {
            logger.info("Counts for #" + hashtag + ": tweets - " + nTweets +
                            ", retweets - {}, goal tweets - {}, goals - {}, extra letters - {}",
                    nRetweets, nGoalTweets, nGoals, nExtraLetters);
            //getSender().tell(new CountsReply(nTweets, nRetweets, nGoalTweets, nGoals, nExtraLetters), getSelf());
        } else {
            unhandled(message);
        }
    }

}
