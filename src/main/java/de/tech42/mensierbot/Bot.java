package de.tech42.mensierbot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Hello world!
 *
 */
public class Bot {

    private final String codename = "rabbit nom noms carrots";
    
    private void tweet(Twitter twitter, String tweet) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String tweetEnd = " (Stand " + sdf.format(new Date()) + ")";
        tweet += tweetEnd;
        try {
            while (tweet.length() > 140) {

                String toTweet = tweet.subSequence(0, 140).toString();
                String restToTweet = tweet.subSequence(141, tweet.length()).toString();
                twitter.updateStatus(toTweet);
                System.out.println("> " + toTweet);
                tweet = restToTweet;

            }

            twitter.updateStatus(tweet);
            System.out.println("> " + tweet);
        } catch (TwitterException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runBot(String user) {
        
        System.setProperty("twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory");
        //String botUsername = "mensierbdev";
        String botUsername = user;
        
        System.out.println("MensierBot starting up!");
        System.out.println("Codename: " + this.codename);

        DB db = new DB("data.db");
        List<String> admins = db.getAdmins();

        
        BotSettings bs = db.readSettings(botUsername);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setDebugEnabled(false)
                .setOAuthConsumerKey(bs.oAuthConsumerKey)
                .setOAuthConsumerSecret(bs.oAuthConsumerSecret)
                .setOAuthAccessToken(bs.oAuthAccessToken)
                .setOAuthAccessTokenSecret(bs.oAuthAccessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();


        long sinceId = bs.lastTweet;
        boolean stop = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY");
        while (!stop) {
            try {
                boolean postStatus = false;
                String datum = sdf.format(new Date());
                List<Status> tweets = twitter.getMentionsTimeline(new Paging(sinceId));
                ListIterator<Status> li = tweets.listIterator(tweets.size());
                while (li.hasPrevious()) {
                    Status status = li.previous();
                    String username = status.getUser().getScreenName();
                    String text = status.getText();




                    // Da wir nur Mentions lesen, den Tweet "aufräumen"
                    //text = text.subSequence(2 + bs.username.length(), text.length()).toString();
                    text = text.replaceAll("(?i)@" + bs.username + " ", "");

                    System.out.println("[" + username + "] " + text);
                    if (status.getId() > sinceId) {
                        sinceId = status.getId();
                        // Wir müssen die DB updaten
                        db.updateLastTweet(sinceId);
                    }

                    if (text.matches("status")) {
                        String tweet = this.processStatus(db, username, datum, text);

                        tweet(twitter, tweet);
                    } else if (text.matches("(?i)status .*")) {
                        String tweet = this.processStatusParameter(db, username, datum, text);

                        tweet(twitter, tweet);
                    } else if (text.matches("(?i)ja .*")) {
                        String tweet = this.processJa(db, username, datum, text);

                        tweet(twitter, tweet);
                        postStatus = true;
                    } else if (text.matches("(?i)nein .*")) {

                        String tweet = this.processNein(db, username, datum, text);

                        tweet(twitter, tweet);
                        postStatus = true;
                    } else if (text.matches("(?i)nein alle")) {

                        String tweet = this.processNeinAlle(db, username, datum, text);
                        tweet(twitter, tweet);
                        postStatus = true;
                    } else if (text.matches("(?i)psa .*")) {
                        String tweet;

                        if (admins.contains(username)) {
                            tweet = this.processPSA(db, username, datum, text);

                        } else {
                            tweet = "@" + username + " Nee du lass mal...";
                        }
                        tweet(twitter, tweet);

                    } else if (text.matches("(?i)auswahl [0-2][0-9]:?[0-5][0-9]")) {

                        String tweet = this.processAuswahl(db, username, datum, text);

                        tweet(twitter, tweet);
                        postStatus = true;
                    }

                }

                if (postStatus) {

                    String tweet = this.processStatusPublic(db, "", datum, "");

                    tweet(twitter, tweet);
                }
            } catch (TwitterException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }




    }

    // ------- COMMANDS ----------- //
    private String processStatusPublic(DB db, String username, String datum, String text) {
        MensaZeit mz = db.getMensaZeit(datum);
        String tweet = "Mensier Status: ";
        boolean somethingWritten = false;
        int cnt = 0;
        for (Map.Entry<String, List<String>> entry : mz.entrySet()) {
            cnt++;
            somethingWritten = true;
            tweet += entry.getKey() + " [" + entry.getValue().size() + "]";
            if (cnt >= mz.size()) {
                tweet += " ";
            } else {
                tweet += ", ";
            }
        }
        if (!somethingWritten) {
            tweet += "Niemand hat Hunger :-(";
        }

        return tweet;

    }

    private String processStatus(DB db, String username, String datum, String text) {


        String tweet = "@" + username + " " + this.processStatusPublic(db, username, datum, text);

        return tweet;
    }

    private String processStatusParameter(DB db, String username, String datum, String text) {
        List<String> toProcess = this.getTime(text);



        String tweet = "@" + username + " Mensier Status: ";
        MensaZeit mz = db.getMensaZeit(datum);
        for (String zeit : toProcess) {
            tweet += "[" + zeit + "] ";

            if (mz.get(zeit) == null) {
                tweet += "Niemand! ";
            } else {
                for (String u : mz.get(zeit)) {
                    tweet += u + " ";
                }
            }
        }
        return tweet;
    }

    private String processJa(DB db, String username, String datum, String text) {
        for(String zeit : this.getTime(text))
        {
            db.setYes(username, datum, zeit);
        }
        return "@" + username + " Allet klar chef";
    }

    private String processNein(DB db, String username, String datum, String text) {
        
        for(String zeit : this.getTime(text))
        {
            db.setNo(username, datum, zeit);
        }
        return "@" + username + " Schade, aber schon okay!";
    }

    private String processNeinAlle(DB db, String username, String datum, String text) {
        db.setNoAlle(username, datum);

        return "@" + username + " Wer nicht will der hat schon...";
    }

    private String processPSA(DB db, String username, String datum, String text) {
        return text.substring(4, text.length());
    }

    private String processAuswahl(DB db, String username, String datum, String text) {
        String zeit = this.getTime(text, 1).get(0);
        
        String tweet = "@" + username + " Yay, du hast dich entschieden!";
        db.setNoAlle(username, datum);
        db.setYes(username, datum, zeit);
        
        return tweet;
    }
    private List<String> getTime(String text)
    {
        return this.getTime(text, -1);
    }
    private List<String> getTime(String text, int limit)
    {
        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("[0-2][0-9]:?[0-5][0-9]").matcher(text);
        while (m.find() && (limit==-1 || limit>=list.size())) {
            String zeit = m.group();
            if (zeit.length() == 4) {
                zeit = zeit.substring(0, 2) + ":" + zeit.substring(2, 4);
            }
            list.add(zeit);
        }
        
        return list;
    }
}
