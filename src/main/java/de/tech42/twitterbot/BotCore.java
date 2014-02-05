/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.twitterbot;

import twitter4j.Status;

/**
 *
 * @author dan
 */
public abstract class BotCore {
    protected TwitterBot twitterBot;
    

    public void assignTwitterBot(TwitterBot twitterBot)
    {
        this.twitterBot = twitterBot;
    }
    public abstract void react(Status[] status);
    public abstract void initalizeBot();
}
