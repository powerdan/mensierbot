/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.mensierbot;

import de.tech42.twitterbot.BotCore;
import de.tech42.twitterbot.TwitterBot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Status;

/**
 *
 * @author dan
 */
public class MensierBot extends BotCore {

    private SimpleDateFormat sdf;
    private List<String> admins;
    private DB db;
    private ReminderThread rt;

    public void initalizeBot() {
        this.sdf = new SimpleDateFormat("dd.MM.YYYY");
        this.db = new DB("data.db");
        this.admins = db.getAdmins();
        this.rt = new ReminderThread(this, this.db);
        this.rt.start();
    }

    public void tweet(String tweet) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String tweetEnd = " (Stand " + sdf.format(new Date()) + ")";
        // Absteigende Prio wo getrennt werden soll
        // Das splitAtShift gibt an, an welchem Zeichen dann getrennt werden soll
        String[] splitAtStr = {",", " "};
        int[] splitAtShift = {1, 0};

        String mentionFor = null;
        boolean isMention = false;
        if (tweet.charAt(0) == '@') {
            mentionFor = tweet.split(" ")[0].substring(1);
            tweet = tweet.substring(mentionFor.length() + 2);
            isMention = true;
        }


        int myLength = 140 - tweetEnd.length();
        if (isMention) {
            myLength -= (mentionFor.length() + 2);
        }

        while (tweet.length() > myLength) {
            int splitAt = myLength;
            int splitShift = 0;

            String mySplitAt = null;
            int idx = 0;
            for (String s : splitAtStr) {
                if (tweet.contains(s)) {
                    mySplitAt = s;
                    splitShift = splitAtShift[idx];
                    break;
                }
                idx++;
            }
            if (mySplitAt != null) {
                splitAt = tweet.lastIndexOf(mySplitAt);

                while (splitAt + splitShift > myLength) {

                    splitAt = tweet.subSequence(0, splitAt).toString().lastIndexOf(mySplitAt);
                }

                splitAt += splitShift;
            }
            String toTweet = tweet.subSequence(0, splitAt).toString();
            String restToTweet = tweet.subSequence(splitAt, tweet.length()).toString();

            if (isMention) {
                this.twitterBot.tweet("@" + mentionFor + " " + toTweet + tweetEnd);
            } else {
                this.twitterBot.tweet(toTweet + tweetEnd);
            }

            tweet = restToTweet;

        }

        if (isMention) {
            this.twitterBot.tweet("@" + mentionFor + " " + tweet + tweetEnd);
        } else {
            this.twitterBot.tweet(tweet + tweetEnd);
        }



    }

    public void react(Status[] status) {
        boolean postStatus = false;
        String datum = sdf.format(new Date());

        for (Status s : status) {
            String username = s.getUser().getScreenName();
            String text = s.getText();


            text = text.replaceAll("(?i)@" + this.twitterBot.getUsername() + " ", "");

            if (text.matches("status")) {
                String tweet = this.processStatus(db, username, datum, text);

                tweet(tweet);
            } else if (text.matches("(?i)status .*")) {
                String tweet = this.processStatusParameter(db, username, datum, text);

                tweet(tweet);
            } else if (text.matches("(?i)ja alle.*")) {

                String tweet = this.processJaAlle(db, username, datum, text);
                tweet(tweet);
                postStatus = true;
            } else if (text.matches("(?i)ja .*")) {
                String tweet = this.processJa(db, username, datum, text);

                tweet(tweet);
                postStatus = true;
            } else if (text.matches("(?i)nein alle.*")) {

                String tweet = this.processNeinAlle(db, username, datum, text);
                tweet(tweet);
                postStatus = true;
            } else if (text.matches("(?i)nein .*")) {

                String tweet = this.processNein(db, username, datum, text);

                tweet(tweet);
                postStatus = true;
            } else if (text.matches("(?i)psa .*")) {
                String tweet;

                if (this.admins.contains(username)) {
                    tweet = this.processPSA(db, username, datum, text);

                } else {
                    tweet = "@" + username + " Nee du lass mal...";
                }
                tweet(tweet);

            } else if (text.matches("(?i)auswahl [0-2][0-9]:?[0-5][0-9]")) {

                String tweet = this.processAuswahl(db, username, datum, text);

                tweet(tweet);
                postStatus = true;
            } else if (text.matches("(?i)reminder (ja|yes|jau|jop|true|1|an|on)")) {
                String tweet = this.processReminderJa(db, username, datum, text);
                tweet(tweet);
            } else if (text.matches("(?i)reminder (nein|no|nope|nee|false|0|aus|off)")) {
                String tweet = this.processReminderNein(db, username, datum, text);
                tweet(tweet);
            }
        }

        if (postStatus) {

            String tweet = this.processStatusPublic(db, "", datum, "");

            tweet(tweet);
        }
    }

    // ------------------- HILFSMETHODEN ------------------- //
    private List<String> getTime(String text) {
        return this.getTime(text, -1);
    }

    private List<String> getTime(String text, int limit) {
        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("[0-2][0-9]:?[0-5][0-9]").matcher(text);
        while (m.find() && (limit == -1 || limit >= list.size())) {
            String zeit = m.group();
            if (zeit.length() == 4) {
                zeit = zeit.substring(0, 2) + ":" + zeit.substring(2, 4);
            }
            list.add(zeit);
        }

        return list;
    }
    // ------------------- LOGIK ------------------- //

    private String processStatusPublic(DB db, String username, String datum, String text) {
        MensaZeit mz = db.getMensaZeit(datum);
        String tweet = "Mensier Status: ";
        boolean somethingWritten = false;
        int cnt = 0;
        List<String> data = new ArrayList<String>();
        for (Map.Entry<String, List<String>> entry : mz.entrySet()) {
            data.add(entry.getKey());
        }

        Collections.sort(data);

        for (String time : data) {
            List<String> entry = mz.get(time);
            cnt++;
            somethingWritten = true;
            tweet += time + " [" + entry.size() + "]";
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
        for (String zeit : this.getTime(text)) {
            db.setYes(username, datum, zeit);
        }
        return "@" + username + " Alles klar, Chef!";
    }

    private String processNein(DB db, String username, String datum, String text) {

        for (String zeit : this.getTime(text)) {
            db.setNo(username, datum, zeit);
        }
        return "@" + username + " Schade, aber schon okay!";
    }

    private String processNeinAlle(DB db, String username, String datum, String text) {
        db.setNoAlle(username, datum);

        return "@" + username + " Wer nicht will der hat schon...";
    }

    private String processJaAlle(DB db, String username, String datum, String text) {
        db.setYesAlle(username, datum);

        return "@" + username + " Oki doki - Du bist ganz schön flexibel.";
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

    private String processReminderJa(DB db, String username, String datum, String text) {
        String tweet = "@" + username + " Okay, Reminder aktiviert";
        db.setReminder(username, 1);
        return tweet;
    }

    private String processReminderNein(DB db, String username, String datum, String text) {
        String tweet = "@" + username + " Okay, Reminder deaktiviert";
        db.setReminder(username, 0);
        return tweet;
    }
}
