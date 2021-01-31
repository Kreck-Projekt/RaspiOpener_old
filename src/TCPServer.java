import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

class TCPServer {
    private static boolean secured = false;
    static String key;
    static String nonce;
    static String oriHash = "";  //original hash, just saved here for testing purposes
    static String tHash; //transmitted hash

    public static void run() throws Exception {
        File myObj = new File("storage.txt");
        DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Scanner sc = new Scanner(myObj);
        try{
            key = sc.nextLine();
            oriHash = sc.nextLine();
        }
        catch(Exception e){
            e.printStackTrace();
            secured = true;
        }
        String fromclient;

        // first startup
        boolean fsu = true;

        ServerSocket Server = new ServerSocket(5000);

        System.out.println("TCPServer waiting for client on port 5000 ");
        Printer.printToFile(dateF.format(new Date()) + ": Server starts", "log.txt", true);
        while (true) {
            Socket connected = Server.accept();
            System.out.println("Client at " + " " + connected.getInetAddress()+ ":" + connected.getPort() + " connected ");

            BufferedReader fromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));

            PrintWriter toClient = new PrintWriter(connected.getOutputStream(), true);
            if(fsu){
                toClient.println("BUT NOT FOR ME");
                fsu = false;
            }
            toClient.println("Connected");
            // receive from app
            fromclient = fromClient.readLine();
            System.out.println("Recieved: " + fromclient);
            try{
                if (fromclient.charAt(1) != ':') {
                    connected.close();
                    continue;
                } //checks if the sent message is a command#
            }
            catch (Exception e){
                connected.close();
                continue;
            }
            if (fromclient.charAt(0) != 'H') Printer.printToFile(dateF.format(new Date()) + ": Client at: " + connected.getInetAddress() + " sent " + fromclient.charAt(0) + " command", "log.txt", true);
            String param = fromclient.substring(2);
            boolean first /*the first found semicolon*/ = false;
            for (int i = 0; i < param.length(); i++) {
                if (!first && param.charAt(i) == ';') first = true;
                else if (first && param.charAt(i) == ';') nonce = param.substring(i + 1);
            }
            System.out.println(fromclient);
            int posPas = -1;
            switch (fromclient.charAt(0)) {
                case 'n':
                    break;
                case 'k':
                    if (((key == null || key.equals("")) && param.length() == 32) && secured) {
                        key = param;
                        Printer.printToFile(key, "storage.txt", false);
                        Printer.printToFile(dateF.format(new Date()) + ": Key set to: " + key, "log.txt", true);
                    }
                    break;
                case 'p':
                    for (int i = 0; i < param.length(); i++) {
                        if (param.charAt(i) == ';') {
                            nonce = param.substring(i + 1);
                            param = param.substring(0, i);
                        }
                    }
                    oriHash = Decryption.decrypt(key, nonce, param);
                    Printer.printToFile(oriHash, "storage.txt", true);
                    Printer.printToFile(dateF.format(new Date()) + ": The password hash was set to: " + oriHash, "log.txt", true);
                    break;
                case 'c':
                    String nHash = null;
                    String hashes = null;
                    for (int i = 0; i < param.length(); i++) {
                        if (param.charAt(i) == ';') {
                            nonce = param.substring(i + 1);
                            hashes = param.substring(0, i);
                        }
                    }
                    System.out.println(nonce);
                    param = Decryption.decrypt(key, nonce, hashes);
                    for (int i = 0; i < param.length(); i++) {
                        if (param.charAt(i) == ';') {
                            nHash = param.substring(i + 1);
                            tHash = param.substring(0, i);
                        }
                    }
                    if (hashCheck(tHash)) {
                        oriHash = nHash;
                        Printer.printToFile(key + "\n" + nHash, "storage.txt", false);
                        Printer.printToFile(dateF.format(new Date()) + ": Password hash was changed to: " + nHash, "log.txt", true);
                    }
                    break;
                case 'a': // a für "password action" aka halts maul justin und formulier gescheit was du sagen willst du keks
                    System.out.println("PaSsWoRd AcTiOn"); // this case is irrelevant
                    System.out.println("Junge sag doch einfach, dass das als öffnen gemeint war");
                    Printer.printToFile(dateF.format(new Date()) + ": Das hätte definitiv nicht passieren sollen? #weirdflexbutok", "log.txt", true);
                    break;
                case 'o': // O für open
                    for (int i = 0; i < param.length(); i++) {
                        if (!first && param.charAt(i) == ';') first = true;
                        else if (first && param.charAt(i) == ';') {
                            nonce = param.substring(i + 1);
                            param = param.substring(0, i);
                        }
                    }
                    String dcrMsg = Decryption.decrypt(key, nonce, param);

                    for (int i = 0; i < dcrMsg.length(); i++) {
                        if (dcrMsg.charAt(i) == ';') {
                            posPas = i;
                            break;
                        }
                    }
                    if (hashCheck(dcrMsg.substring(0, posPas))) {
                        System.out.println("Door is being opened...\n");
                        GpioController.activate(Integer.parseInt(dcrMsg.substring(posPas + 1)));
                        Printer.printToFile(dateF.format(new Date()) + ": Door is being opened","log.txt", true);
                    } else {
                        System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
                        Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", "log.txt", true);
                        toClient.println("Wrong password");
                    }
                    break;
                case 'r':
                    for (int i = 0; i < param.length(); i++) {
                        if (!first && param.charAt(i) == ';') first = true;
                        else if (first && param.charAt(i) == ';') {
                            nonce = param.substring(i + 1);
                            param = param.substring(0, i);
                        }
                    }
                    String dcrHsh = Decryption.decrypt(key, nonce, param);

                    for (int i = 0; i < dcrHsh.length(); i++) {
                        if (dcrHsh.charAt(i) == ';') {
                            posPas = i;
                            break;
                        }
                    }
                    if (hashCheck(dcrHsh.substring(0, posPas))) {
                        System.out.println("Door is being opened...\n");
                        Printer.printToFile(dateF.format(new Date()) + ": The Pi was reset from IP address: " + connected.getInetAddress(), "log.txt", true);
                        BashIn.exec("sudo rm storage.txt");
                        BashIn.exec("sudo touch storage.txt");
                    } else {
                        System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
                        Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", "log.txt", true);
                        toClient.println("Wrong password");
                    }
                    break;
                case 'H':
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

        }


    private static boolean hashCheck (String tHash){
        return tHash.equals(/*hash aus speicher?*/oriHash);
    }

}
