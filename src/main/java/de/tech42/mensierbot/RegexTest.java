/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tech42.mensierbot;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author dan
 */
public class RegexTest {
    public static void main(String[] args)
    {
        Scanner scin = new Scanner(System.in);
        
        while(true)
        {
            String regex = "status ([0-2][0-9]:[0-5][0-9]( )?)+";
            System.out.print("in: ");
            String line = scin.nextLine();
            System.out.println("read: " + line);
            System.out.println("regex: " + regex);
            System.out.println("matches: " + line.matches(regex));
            if(line.matches(regex))
            {
                 Matcher m = Pattern.compile("[0-2][0-9]:[0-5][0-9]").matcher(line);
                while (m.find()) {
                    System.out.println("  " + m.group());
                }
            }
            else
            {
                System.out.println("cant display data");
            }
            System.out.println("-----------------");
        }
    }
}
