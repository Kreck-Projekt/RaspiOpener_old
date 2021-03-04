package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

class TCPServer {
    private static boolean secured = false;
    static String key;
    static String nonce;
    static String oriHash = "";  //original hash, just saved here for testing purposes
    static String tHash; //transmitted hash
    static List<String> otps;
    private static Handler handler;

    public static void run() throws Exception {
        File keyPasStore = new File("keyPasStore.txt");
        File otpStore = new File("otpStore.txt");
        DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Scanner kpsc = new Scanner(keyPasStore);
        Scanner otpscan = new Scanner(otpStore);
        try{
            key = kpsc.nextLine();
            oriHash = kpsc.nextLine();
        }
        catch(Exception e){
            e.printStackTrace();
            secured = true;
        }
        otps = new ArrayList<>();
        while(true){
            try{
                otps.add(otpscan.nextLine());
            }
            catch(Exception e){
                e.printStackTrace();
                break;
            }

        }

        handler.key = key;
        handler.oriHash = oriHash;

        String fromclient;

        // first startup
        boolean fsu = true;

        ServerSocket Server = new ServerSocket(5000);

        System.out.println("de.NikomitK.RaspiOpener.main.TCPServer waiting for client on port 5000 ");
        Printer.printToFile(dateF.format(new Date()) + ": Server starts", "log.txt", true);
        while (true) {


            try{
                Socket connected = Server.accept();
                System.out.println("Client at " + " " + connected.getInetAddress() + ":" + connected.getPort() + " connected ");

                BufferedReader fromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));

                PrintWriter toClient = new PrintWriter(connected.getOutputStream(), true);
                if (fsu) {
                    toClient.println("BUT NOT FOR ME");
                    fsu = false;
                }
                toClient.println("Connected");
                // receive from app
                fromclient = fromClient.readLine();
                System.out.println("Recieved: " + fromclient);
                try {
                    if (fromclient.charAt(1) != ':') {
                        connected.close();
                        continue;
                    } //checks if the sent message is a command#
                } catch (Exception e) {
                    connected.close();
                    continue;
                }
                if (fromclient.charAt(0) != 'H')
                    Printer.printToFile(dateF.format(new Date()) + ": Client at: " + connected.getInetAddress() + " sent " + fromclient.charAt(0) + " command", "log.txt", true);
                String param;
                try {
                    param = fromclient.substring(2);
                } catch (Exception e) {
                    e.printStackTrace();
                    connected.close();
                    break;
                }
                boolean first /*the first found semicolon*/ = false;
                for (int i = 0; i < param.length(); i++) {
                    if (!first && param.charAt(i) == ';') first = true;
                    else if (first && param.charAt(i) == ';') nonce = param.substring(i + 1);
                }
                int posPas = -1;


                switch (fromclient.charAt(0)) {
                    case 'n': //irrelevant
                        break;

                    case 'k': //storeKey done
                        // Command syntax: "k:<key>"
                        if (((key == null || key.equals("")) && param.length() == 32) && secured)
                            handler.storeKey(param);
                            key = handler.key;
                        break;

                    case 'p': //storePW done
                        // Command syntax: "p:(<hash>);<nonce>"
                        boolean pwStored = handler.storePW(param);
                        if(!pwStored) System.out.println("Das hat nicht geklappt! :(");
                        oriHash = handler.oriHash;
                        break;
//                        for (int i = 0; i < param.length(); i++) {
//                            if (param.charAt(i) == ';') {
//                                nonce = param.substring(i + 1);
//                                param = param.substring(0, i);
//                            }
//                        }
//                        oriHash = Decryption.decrypt(key, nonce, param);
//                        Printer.printToFile(oriHash, "keyPasStore.txt", true);
//                        Printer.printToFile(dateF.format(new Date()) + ": The password hash was set to: " + oriHash, "log.txt", true);

                    case 'c': //changePW done
                        // Command syntax: "c:(<oldHash>;<newHash>);<nonce>"
                        boolean changed = handler.changePW(param);
                        if(!changed) System.out.println("Das hat nicht geklappt! :(");
                        break;
//                        String nHash = null;
//                        String hashes = null;
//                        for (int i = 0; i < param.length(); i++) {
//                            if (param.charAt(i) == ';') {
//                                nonce = param.substring(i + 1);
//                                hashes = param.substring(0, i);
//                            }
//                        }
//                        System.out.println(nonce);
//                        param = Decryption.decrypt(key, nonce, hashes);
//                        for (int i = 0; i < param.length(); i++) {
//                            if (param.charAt(i) == ';') {
//                                nHash = param.substring(i + 1);
//                                tHash = param.substring(0, i);
//                            }
//                        }
//                        if (hashCheck(tHash)) {
//                            oriHash = nHash;
//                            Printer.printToFile(key + "\n" + nHash, "keyPasStore.txt", false);
//                            Printer.printToFile(dateF.format(new Date()) + ": Password hash was changed to: " + nHash, "log.txt", true);
//                        }

                    case 's': // setOTP done
                        // Command syntax "s:(<otp>;<hash>);nonce"
                        boolean otpSet = handler.setOTP(param);
                        if(!otpSet) System.out.println("Das hat nicht geklappt! :(");
                        otps = handler.otps;
                        break;
//                        for (int i = 0; i < param.length(); i++) {
//                            if (!first && param.charAt(i) == ';') first = true;
//                            else if (first && param.charAt(i) == ';') {
//                                nonce = param.substring(i + 1);
//                                param = param.substring(0, i);
//                            }
//                        }
//                        String dcrOTP = Decryption.decrypt(key, nonce, param);
//                        for (int i = 0; i < dcrOTP.length(); i++) {
//                            if (dcrOTP.charAt(i) == ';') {
//                                posPas = i;
//                                break;
//                            }
//                        }
//                        try{
//                            Printer.printToFile(dcrOTP + "\n", "otpStore.txt", true);
//                            otps.add(dcrOTP);
//                        }
//                        catch(FileNotFoundException fnfe){
//                            BashIn.exec("sudo touch otpStore.txt");
//                        }
//                        catch (Exception e){
//                            e.printStackTrace();
//                        }

                    case 'e': // einmalöffnung done
                        // Command syntax: "e:<otp>;<time>"
                        boolean otOpened = handler.einmalOeffnung(param);
                        if(!otOpened) System.out.println("Das hat nicht geklappt! :(");
                        otps = handler.otps;
                        break;
//                        String openTime = null;
//                        for (int i = 0; i < param.length(); i++) {
//                            if (!first && param.charAt(i) == ';') first = true;
//                            else if (first && param.charAt(i) == ';') {
//                                openTime = param.substring(i + 1);
//                                param = param.substring(0, i);
//                            }
//                        }
//                        int remPos;
//                        for (int i = 0; i<otps.size(); i++) {
//                            if(otps.get(i) == param){
//                                System.out.println("Door is being opened by OTP...");
//                                GpioController.activate(Integer.parseInt(openTime));
//                                Printer.printToFile(dateF.format(new Date()) + ": Door is being opened with OTP", "log.txt", true);
//                                otps.remove(i);
//                                try {
//                                    BashIn.exec("sudo rm otpStore.txt");
//                                    BashIn.exec("sudo touch otpStore.txt");
//                                    for (int j = 0; j < otps.size(); j++) {
//                                        Printer.printToFile(otps.get(j), "otpStore.txt", true);
//                                    }
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//                            else{
//                                System.out.println("Client used a wrong OTP");
//                                Printer.printToFile(dateF.format(new Date()) + ": A wrong OTP has been used", "log.txt", true);
//                            }
//                        }

                    case 'a': // a für "password action" aka halts maul justin und formulier gescheit was du sagen willst du keks
                        System.out.println("PaSsWoRd AcTiOn"); // this case is irrelevant
                        System.out.println("Junge sag doch einfach, dass das als öffnen gemeint war"); // just ignore this
                        Printer.printToFile(dateF.format(new Date()) + ": Das hätte definitiv nicht passieren sollen? #weirdflexbutok", "log.txt", true);
                        break;

                    case 'o': // Open  done
                        // Command syntax: "o:(<hash>;<time>);<nonce>"
                        boolean opened = handler.open(param);
                        if(!opened) System.out.println("Das hat nicht geklappt! :(");
                        break;
//                        for (int i = 0; i < param.length(); i++) {
//                            if (!first && param.charAt(i) == ';') first = true;
//                            else if (first && param.charAt(i) == ';') {
//                                nonce = param.substring(i + 1);
//                                param = param.substring(0, i);
//                            }
//                        }
//                        String dcrMsg = Decryption.decrypt(key, nonce, param);
//
//                        for (int i = 0; i < dcrMsg.length(); i++) {
//                            if (dcrMsg.charAt(i) == ';') {
//                                posPas = i;
//                                break;
//                            }
//                        }
//                        if (hashCheck(dcrMsg.substring(0, posPas))) {
//                            System.out.println("Door is being opened...");
//                            GpioController.activate(Integer.parseInt(dcrMsg.substring(posPas + 1)));
//                            Printer.printToFile(dateF.format(new Date()) + ": Door is being opened", "log.txt", true);
//                            Socket desktop = new Socket("192.168.0.2", 6969);
//                            PrintWriter toDesktop = new PrintWriter(connected.getOutputStream(), true);
//                            toDesktop.println("Opened");
//                            desktop.close();
//                        } else {
//                            System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
//                            Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", "log.txt", true);
//                            toClient.println("Wrong password");
//                        }

                    case 'r': //reset
                        // Command syntax: "r:(<hash>);<nonce>"
                        boolean isReset = handler.reset(param);
                        key = handler.key;
                        oriHash = handler.oriHash;
                        break;
//                        for (int i = 0; i < param.length(); i++) {
//                            if (!first && param.charAt(i) == ';') first = true;
//                            else if (first && param.charAt(i) == ';') {
//                                nonce = param.substring(i + 1);
//                                param = param.substring(0, i);
//                            }
//                        }
//                        String dcrHsh = Decryption.decrypt(key, nonce, param);
//
//                        for (int i = 0; i < dcrHsh.length(); i++) {
//                            if (dcrHsh.charAt(i) == ';') {
//                                posPas = i;
//                                break;
//                            }
//                        }
//                        if (hashCheck(dcrHsh.substring(0, posPas))) {
//                            System.out.println("Pi is getting reset...\n");
//                            Printer.printToFile(dateF.format(new Date()) + ": The Pi was reset from IP address: " + connected.getInetAddress(), "log.txt", true);
//                            BashIn.exec("sudo rm keyPasStore.txt");
//                            BashIn.exec("sudo touch keyPasStore.txt");
//                            BashIn.exec("sudo rm otpStore.txt");
//                            BashIn.exec("sudo touch otpStore.txt");
//                        } else {
//                            System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
//                            Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", "log.txt", true);
//                            toClient.println("Wrong password");
//                        }

                    case 'H': // "how are you", get's called from alivekeeper, never from user lmao
                        toClient.println("I'm fine, thanks");
                        break;


                    default:
                        //also irrelevant
                        System.out.println("hat wohl nich gegeht ¯\\_(ツ)_/¯ ");
                        Printer.printToFile(dateF.format(new Date()) + ": Wie zur Hölle bist du hier gelandet??? #nochweirdererflex", "log.txt", true);
                        Printer.printToFile(dateF.format(new Date()) + ": The whole message was: " + fromclient, "log.txt", true);
                        toClient.println("What do you want from this poor Server? \uD83E\uDD7A");
                        break;
                }


                //Justin wollte iwie ne nachricht idk

                try {
                    toClient.println("Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Komplett Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen\n");
                } catch (Exception e) {
                    System.out.println("HIER BIN ICH GECRASHT");
                }

                connected.close();
            }


            catch(Exception e){
                e.printStackTrace();
            }

            }

        }


}
