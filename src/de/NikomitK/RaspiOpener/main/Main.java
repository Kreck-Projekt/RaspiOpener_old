package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.*;
import lombok.Getter;
import yapion.parser.YAPIONParser;
import yapion.serializing.YAPIONDeserializer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Main {

    // I wrote most of this code at the beginning of 2021 after I didn't program for a few months because of an accident
    // and I am shocked of the piece of garbage that I produced. This is getting a complete overhaul or else I'll
    // stop programming forever?

    @Getter
    private static Storage storage;
    @Getter
    private static Logger logger;
    @Getter
    private static File storageFile;
    @Getter
    private static boolean debug = false;


    // writing this again, because it's trash
    public static void main(String [] args){
        try{
            logger = new Logger(new File("log.txt"));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Logger couldn't be created. \n Terminating...");
            System.exit(0);
        }
        argHandling(args);
        storageFile = new File("storage.yapion");
        if(storageFile.exists()){
            try{
                storage = (Storage) YAPIONDeserializer.deserialize(YAPIONParser.parse(storageFile));
            } catch (IOException e) {
                logger.error("Unable to read storage. \n Terminating...");
            }
        } else {
            storage = new Storage();
        }
        try{
            //TCPServer.run();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Server Crashed.");
        }
    }
//    public static void main(String[] args) throws Exception {
//        logger = new Logger(new File("log.txt"));
//        if (args.length > 0 && args[0].equals("-r")) {
//            BashIn.exec("sudo rm keyPasStore.txt");
//            BashIn.exec("sudo rm nonceStore.txt");
//        } else if (args.length > 0 && args[0].equals("-debug")) {
//            debug = true;
//        }
//        // gets removed in the next update, just here in case the replacement doesn't work reliably C:
//        // spoiler: it didn't work :C
//        // this method works with testing if a scanner can be created for these files
//        // and if not uses a bash command to create them.
//        // new method will hopefully be implemented in release 2.0
////        try {
////            Scanner sc = new Scanner(new File("keyPasStore.txt"));
////        } catch (FileNotFoundException e) {
////            BashIn.exec("sudo touch keyPasStore.txt");
////        }
////        try {
////            Scanner sc = new Scanner(new File("otpStore.txt"));
////        } catch (FileNotFoundException e) {
////            BashIn.exec("sudo touch otpStore.txt");
////        }
////        try {
////            Scanner sc = new Scanner(new File("nonceStore.txt"));
////        } catch (FileNotFoundException e) {
////            BashIn.exec("sudo touch nonceStore.txt");
////        }
//        // new File("keyPasStore.txt").createNewFile();
//        // new File("otpStore.txt").createNewFile();
//        // new File("nonceStore.txt").createNewFile();
//        try {
//            System.out.println("Starting...");
//            // TCP Server starten...
//            TCPServer.run(debug);
//        } catch (Exception e) {
//            e.printStackTrace(pw);
//            System.out.println("Closing...?");
//            //de.NikomitK.RaspiOpener.handler.Printer.printToFile(dateF.format(new Date()) + ": server crashed?" + sw.toString(), new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
//            Printer.printToFile(dateF.format(new Date()) + ": server crashed? " + sw, "log.txt", true);
//        }
//
//    }

    private static void argHandling (String [] args){

        Set<String> arguments = new HashSet<>();
        for(String s : args) {
            arguments.add(isArgument(s));
        }

        help(arguments.contains("h"));
        debug = arguments.contains("d");
        logger.debug("Debug turnt on");
        resetStorage(arguments.contains("r"));
    }

    private static String isArgument(String arg){
        if(arg.startsWith("-") && !arg.startsWith("--") && arg.length() == 2){
            return arg.substring(1);
        } else if(arg.startsWith("--") && arg.length() > 3){
            return ((Character) arg.charAt(2)).toString();
        } else {
            return null;
        }
    }

    private static void help(boolean print){
        if(print){
            System.out.println("Help\n");
            System.out.println("-h or --help for this message");
            System.out.println("-d or --debug for debug logs");
            System.out.println("-s or --stacktrace for debug with stacktraces"); // not yet implemented
            System.out.println("-r or --reset for resetting it at the start");
        }
    }

    public static void resetStorage(boolean reset){
        storage = new Storage();
        logger.log("Storage was reset");
    }
}
