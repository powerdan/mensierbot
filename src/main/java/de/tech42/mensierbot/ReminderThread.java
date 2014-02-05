/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.mensierbot;

import de.tech42.twitterbot.TwitterBot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dan
 */
public class ReminderThread extends Thread {

    private MensierBot tb;
    private DB db;

    public ReminderThread(MensierBot tb, DB db) {
        this.tb = tb;
        this.db = db;
    }

    private long calculateToNext() {
        Date now = new Date();
        Date target = new Date();
        long add = 0;
        
        int targetHour = 9;
        int targetMinute = 53;
        int targetSecond = 0;
        
        if (now.getHours() >= targetHour && now.getMinutes() >= targetMinute && now.getSeconds() >= targetSecond) {
            add += 86400;
        }

        target.setHours(targetHour);
        target.setMinutes(targetMinute);
        target.setSeconds(targetSecond);

        long targetSec = target.getTime() / 1000 + add;
        long nowSec = now.getTime() / 1000;

        return targetSec - nowSec;
    }

    public void run() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY");
        while (true) {
            try {
                Thread.sleep(this.calculateToNext() * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ReminderThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Ist ein Wochentag?
            GregorianCalendar ge = new GregorianCalendar();
            int tag = ge.get(Calendar.DAY_OF_WEEK);
            if(tag == Calendar.SATURDAY || tag == Calendar.SUNDAY )
                continue;
            
            // Action!
            List<String> toRemind = db.getReminder();
           
            // Haben sich die Leute schon gemeldet?
            MensaZeit mz = db.getMensaZeit(sdf.format(new Date()));
            
            for(Map.Entry<String, List<String>> entry : mz.entrySet())
            {
                for(String user : entry.getValue())
                {
                    if(toRemind.contains(user))
                    {
                        toRemind.remove(user);
                    }
                }
            }
            
            // Remind!
            for(String user : toRemind)
            {
                String tweet = "@" + user + " Denk daran, deine Mensa Zeit einzutragen! #reminder";
                this.tb.tweet(tweet);
            }
            
        }
    }
}
