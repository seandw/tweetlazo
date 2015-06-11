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
}
