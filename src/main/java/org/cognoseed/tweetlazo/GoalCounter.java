package org.cognoseed.tweetlazo;

import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import twitter4j.Status;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Takes in tweets, counts # of tweets, goals, and extra letters in those goals.
 */
public class GoalCounter extends UntypedActor {

    public static class Counts { }
    public class CountsReply {
        public final long tweets;
        public final long goalTweets;
        public final long goals;
        public final long extraLetters;

        public CountsReply(long tweets, long goalTweets, long goals, long extraLetters) {
            this.tweets = tweets;
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

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private String hashtag;
    private long nTweets;
    private long nGoalTweets;
    private long nGoals;
    private long nExtraLetters;
    private final Cancellable tick = getContext().system().scheduler().schedule(
            Duration.create(0, TimeUnit.MILLISECONDS),
            Duration.create(5, TimeUnit.SECONDS),
            getSelf(), new Counts(), getContext().dispatcher(), null);

    public GoalCounter(String hashtag) {
        this.hashtag = hashtag;
    }

    @Override
    public void postStop() throws Exception {
        tick.cancel();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Status) {
            boolean encounteredGoal = false;
            Status status = (Status) message;
            String[] words = stripPunctuation(status.getText()).trim().split("\\s+");
            for (String word : words) {
                String stripped = stripDuplicateAdjacents(word);
                if (GOAL_TRANSLATIONS.contains(stripped)) {
                    logger.info("TWEET CONTAINS {}: {}", stripped, word);
                    encounteredGoal = true;
                    ++nGoals;
                    nExtraLetters += word.length() - stripped.length();
                }
            }
            ++nTweets;
            if (encounteredGoal) ++nGoalTweets;
        } else if (message instanceof Counts) {
            logger.info("Counts for #" + hashtag + ": tweets - {}, goal tweets - {}, goals - {}, extra letters - {}",
                    nTweets, nGoalTweets, nGoals, nExtraLetters);
            //getSender().tell(new CountsReply(nTweets, nGoalTweets, nGoals, nExtraLetters), getSelf());
        } else {
            unhandled(message);
        }
    }

    /**
     * Strips out adjacent duplicate characters.
     */
    public static String stripDuplicateAdjacents(String srcStr) {
        if (srcStr == null || srcStr.length() == 0) return "";
        String str = srcStr.toLowerCase();

        StringBuilder buf = new StringBuilder();
        char last = str.charAt(0);
        buf.append(last);
        for (int idx = 1; idx < str.length(); ++idx) {
            char curr = str.charAt(idx);
            if (last != curr) {
                last = curr;
                buf.append(last);
            }
        }
        return buf.toString();
    }

    /**
     * Strips punctuation and accents.
     */
    public static String stripPunctuation(String srcStr) {
        if (srcStr == null) return "";
        String normalized = Normalizer.normalize(srcStr, Normalizer.Form.NFD);
        return normalized.replaceAll("(?U)[\\p{Punct}\\p{InCombiningDiacriticalMarks}]", "");
    }
}
