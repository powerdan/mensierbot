/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.mensierbot;

import de.tech42.twitterbot.PullTwitterBot;
import de.tech42.twitterbot.TwitterBot;

/**
 *
 * @author dan
 */
public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong Usage: java -jar $jar BotUserName");
        } else {

           /* Bot bot = new Bot();
            bot.runBot(args[0]);*/
            TwitterBot tb = new PullTwitterBot();
            tb.setUsername(args[0]);
            tb.appendBotCore(new MensierBot());
            tb.start();
            
        }
    }
}
