/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.mensierbot;

/**
 *
 * @author dan
 */
public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong Usage: java -jar $jar BotUserName");
        } else {

            Bot bot = new Bot();
            bot.runBot(args[0]);
        }
    }
}
