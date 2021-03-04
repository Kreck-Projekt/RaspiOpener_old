package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    private static StringWriter sw = new StringWriter();
    private static PrintWriter pw = new PrintWriter(sw);
    private static DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static void main(String[] args) throws Exception {

        try{
            Scanner sc = new Scanner(new File("keyPasStore.txt"));
        }
        catch (FileNotFoundException e){
            BashIn.exec("sudo touch keyPasStore.txt");
        }
        try{
            Scanner sc = new Scanner(new File("otpStore.txt"));
        }
        catch(FileNotFoundException e){
            BashIn.exec("sudo touch otpStore.txt");
        }
        try{
            System.out.println("Starting...");
            // TCP Server starten...
            TCPServer.run();
        }
        catch(Exception e){
            e.printStackTrace(pw);
            System.out.println("Closing...?");
            //de.NikomitK.RaspiOpener.handler.Printer.printToFile(dateF.format(new Date()) + ": server crashed?" + sw.toString(), new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
            Printer.printToFile(dateF.format(new Date()) + ": server crashed? " + sw.toString(), "log.txt", true);
        }

    }
}
