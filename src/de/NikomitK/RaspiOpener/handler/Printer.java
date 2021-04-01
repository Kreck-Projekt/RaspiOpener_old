package de.NikomitK.RaspiOpener.handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// used for printing things into text files, why should i always use four lines of code when I can use one
public class Printer {

    public static void printToFile(String text, String fileName, boolean append) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, append)));
        pw.println(text);
        pw.flush();
        pw.close();
    }

    public static void printToDebugFile(String text, String fileName, boolean append, boolean debug) throws IOException {
        if(debug){
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, append)));
            pw.println(text);
            pw.flush();
            pw.close();
        }
    }

}
