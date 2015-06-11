package org.cognoseed.tweetlazo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.*;

public class App {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                logger.debug("{}: {}", status.getId(), status.getText());
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            public void onTrackLimitationNotice(int i) {

            }

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {
                logger.warn("STALL WARNING: {}", stallWarning.getMessage());
            }

            public void onException(Exception e) {
                logger.error(e.getMessage());
            }
        };

        TwitterStream stream = new TwitterStreamFactory().getInstance();
        stream.addListener(listener);

        logger.trace("Starting the stream with args: {}", args);
        stream.filter(new FilterQuery(0, new long[] {}, args));
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
}
