package org.cognoseed.tweetlazo;

import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.util.Properties;

public class App {
    public static void main(String[] args) throws IOException {
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                System.out.println(status.getId() + ": " + status.getText());
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            public void onTrackLimitationNotice(int i) {

            }

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {
                System.err.println("WARNING: " + stallWarning.getMessage());
            }

            public void onException(Exception e) {
                System.err.println("EXCEPTION: " + e.getMessage());
            }
        };

        Properties props = new Properties();
        props.load(App.class.getResourceAsStream("/application.properties"));

        TwitterStream stream = new TwitterStreamFactory().getInstance();
        stream.setOAuthConsumer(props.getProperty("oauth.consumerKey"), props.getProperty("oauth.consumerSecret"));
        stream.setOAuthAccessToken(new AccessToken(props.getProperty("oauth.accessToken"), props.getProperty("oauth.accessTokenSecret")));
        stream.addListener(listener);

        stream.filter(new FilterQuery(0, new long[] {}, new String[] {"#BRA", "#KOR"}));
    }
}
