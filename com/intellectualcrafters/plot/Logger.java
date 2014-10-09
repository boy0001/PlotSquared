/*
 * Copyright (c) IntellectualCrafters - 2014.
 * You are not allowed to distribute and/or monetize any of our intellectual property.
 * IntellectualCrafters is not affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 *
 * >> File = Logger.java
 * >> Generated by: Citymonstret at 2014-08-09 01:43
 */

package com.intellectualcrafters.plot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Logging of errors and debug messages.
 * 
 * @author Citymonstret
 * 
 */
public class Logger {

    private static ArrayList<String> entries;
    private static File log;

    public static void setup(File file) {
        log = file;
        entries = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
            reader.close();
        } catch (IOException e) {
            PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + "File setup error Logger#setup");
        }
    }

    public enum LogLevel {
        GENERAL("General"), WARNING("Warning"), DANGER("Danger");
        private String name;

        LogLevel(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static void write() throws IOException {
        FileWriter writer = new FileWriter(log);
        for (String string : entries) {
            writer.write(string + System.lineSeparator());
        }
        writer.close();
    }

    public static void add(LogLevel level, String string) {
        append("[" + level.toString() + "] " + string);
    }

    private static void append(String string) {
        entries.add("[" + new Date().toString() + "]" + string);
    }
}
