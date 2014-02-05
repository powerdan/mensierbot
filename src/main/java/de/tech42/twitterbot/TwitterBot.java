/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.twitterbot;

import java.util.ArrayList;
import java.util.List;
import twitter4j.Status;

/**
 *
 * @author dan
 */
public abstract class TwitterBot extends Thread {
    protected List<BotCore> bots = new ArrayList<BotCore>();
    protected String userName;
    

    public void reactToStatus(Status[] status) {
        for(BotCore bc : this.bots)
        {
            bc.react(status);
        }
    }

    public void appendBotCore(BotCore botCore) {
        this.bots.add(botCore);
        botCore.assignTwitterBot(this);
        botCore.initalizeBot();
    }

    public void setUsername(String username) {
        this.userName = username;
    }
    
    public String getUsername()
    {
        return this.userName;
    }

    public abstract boolean tweet(String status);
    @Override
    public abstract void run();
}
