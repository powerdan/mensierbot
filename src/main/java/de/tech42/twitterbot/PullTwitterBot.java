/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.twitterbot;

import de.tech42.mensierbot.BotSettings;
import de.tech42.mensierbot.DB;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author dan
 */
public class PullTwitterBot extends TwitterBot {

    private Twitter twitter;
    
    @Override
    public boolean tweet(String status) {
        System.out.println("> " + status);
        try {
            this.twitter.updateStatus(status);
        } catch (TwitterException ex) {
            Logger.getLogger(PullTwitterBot.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }


    
    public void run()
    {
        System.out.println("Starting PullTwitterBot Engine!");
        if(this.userName==null)
        {
            System.err.println("No user was supplied!");
            return;
        }
        System.setProperty("twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory");
        
        DB db = new DB("data.db");
        BotSettings bs = db.readSettings(this.userName);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setDebugEnabled(false)
                .setOAuthConsumerKey(bs.oAuthConsumerKey)
                .setOAuthConsumerSecret(bs.oAuthConsumerSecret)
                .setOAuthAccessToken(bs.oAuthAccessToken)
                .setOAuthAccessTokenSecret(bs.oAuthAccessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        
        long sinceId = bs.lastTweet;
        boolean stop = false;
        
        while(!stop)
        {
            try {
                List<Status> tweets = twitter.getMentionsTimeline(new Paging(sinceId));
                ListIterator<Status> li = tweets.listIterator(tweets.size());
                
                List<Status> tweetsForBot = new ArrayList<Status>();
                while (li.hasPrevious()) {
                    Status status = li.previous();
                    String username = status.getUser().getScreenName();
                    String text = status.getText();

                    System.out.println("[" + username + "] " + text);
                    if (status.getId() > sinceId) {
                        sinceId = status.getId();
                        // Wir müssen die DB updaten
                        db.updateLastTweet(sinceId);
                    }
                    
                    tweetsForBot.add(status);
                    
                }
                this.reactToStatus(tweetsForBot.toArray(new Status[tweetsForBot.size()]));
            } catch (TwitterException ex) {
                Logger.getLogger(PullTwitterBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PullTwitterBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    
}
