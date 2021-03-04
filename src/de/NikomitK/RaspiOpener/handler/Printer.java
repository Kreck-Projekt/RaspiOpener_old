package de.NikomitK.RaspiOpener.handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Printer {

    public static void printToFile(String text, String fileName, boolean append) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, append)));
        pw.println(text);
        pw.flush();
        pw.close();
    }

}
