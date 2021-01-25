import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

class TCPServer {
    static String key;
    static String nonce;
    static String oriHash = "";  //original hash, just saved here for testing purposes
    static String tHash; //transmitted hash
    static PrintWriter logWriter;
    private static File myObj;

    public static void main(String args[]) throws Exception {
        try {
            myObj = new File("storage.txt");
        }
        catch(Exception e){
            BashIn.exec("touch storage.txt");
            myObj = new File("storage.txt");
        }
        DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        Scanner sc = new Scanner(myObj);
        try{
            key = sc.nextLine();
            oriHash = sc.nextLine();
        }
        catch(Exception e){
            BashIn.exec("touch storage.txt");
        }
        String fromclient;

        // first startup
        boolean fsu = true;

        ServerSocket Server = new ServerSocket(5000);

        System.out.println("TCPServer waiting for client on port 5000 ");
        Printer.printToFile(dateF.format(date) + ": Server starts", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));

        while (true) {
            Socket connected = Server.accept();
            String outToFile = dateF.format(date) + ": Client at " + connected.getInetAddress() + " connected";
            System.out.println("Client at " + " " + connected.getInetAddress()+ ":" + connected.getPort() + " connected ");
            System.out.println(connected.getInetAddress());
            if(connected.getInetAddress().toString() != "Client at /127.0.0.1 connected\n") {
                Printer.printToFile(dateF.format(date) + ": Client at " + connected.getInetAddress() + " connected", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
            }

            BufferedReader fromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));

            PrintWriter toClient = new PrintWriter(connected.getOutputStream(), true);
            if(fsu){
                toClient.println("BUT NOT FOR ME");
                fsu = false;
            }
            toClient.println("Connected");
            // this loop is probably useless, since it will break after every time it happens, but i don't want to delete it
            while (true) {

                // receive from app
                fromclient = fromClient.readLine();
                System.out.println("Recieved: " + fromclient);
                if(fromclient.charAt(0) != 'H') Printer.printToFile(dateF.format(date) + ": Client sent " + fromclient.charAt(0) + " command", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                String param = fromclient.substring(2);
                boolean first /*the first found semicolon*/ = false;
                for(int i = 0; i<param.length(); i++){
                    if(!first && param.charAt(i) == ';') first = true;
                    else if(first && param.charAt(i) == ';') nonce = param.substring(i+1);
                }
                System.out.println(fromclient);
                int posPas = -1;
                switch (fromclient.charAt(0)) {
                    case 'n':
                        break;
                    case 'k':
                        if(key == null){
                            key = param;
                            Printer.printToFile(key, new PrintWriter(new BufferedWriter(new FileWriter("storage.txt"))));
                            Printer.printToFile(dateF.format(date) + ": Key set to: " + key, new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                        }
                        break;
                    case 'p':
                            for (int i = 0; i < param.length(); i++) {
                                if (param.charAt(i) == ';') {
                                    nonce = param.substring(i + 1);
                                    param = param.substring(0, i);
                                }
                            }
                            String s = Decryption.decrypt(key, nonce, param);
                            oriHash = s;
                            Printer.printToFile(oriHash, new PrintWriter(new BufferedWriter(new FileWriter("storage.txt", true))));
                            Printer.printToFile(dateF.format(date) + ": The password hash was set to: " + oriHash, new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                            break;
                    case 'c':
                        String nHash = null;
                        String hashes = null;
                        for(int i = 0; i<param.length(); i++){
                            if(param.charAt(i) == ';') {
                                nonce = param.substring(i + 1);
                                hashes = param.substring(0, i);
                            }
                        }
                        System.out.println(nonce);
                        param = Decryption.decrypt(key, nonce, hashes);
                        for(int i = 0; i<param.length(); i++){
                            if(param.charAt(i) == ';') {
                                nHash = param.substring(i + 1);
                                tHash = param.substring(0, i);
                            }
                        }
                        if(hashCheck(tHash)) {
                            oriHash = nHash;
                            Printer.printToFile(key + "\n" + nHash, new PrintWriter(new BufferedWriter(new FileWriter("storage.txt"))));
                            Printer.printToFile(dateF.format(date) + ": Password hash was changed to: " + nHash, new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                        }
                        break;
                    case 'a': // a für "password action" aka halts maul justin und formulier gescheit was du sagen willst du keks
                        System.out.println("PaSsWoRd AcTiOn"); // this case is irrelevant
                        System.out.println("Junge sag doch einfach, dass das als öffnen gemeint war");
                        Printer.printToFile(dateF.format(date) + ": Das hätte definitiv nicht passieren sollen? #weirdflexbutok", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                        break;
                    case 'o': // O für open
                        for(int i = 0; i<param.length(); i++){
                            if(!first && param.charAt(i) == ';') first = true;
                            else if(first && param.charAt(i) == ';') {
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
                            Printer.printToFile(dateF.format(date) + ": Door is opened", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                            GpioController.activate(Integer.valueOf(dcrMsg.substring(posPas+1)));
                        }
                        else {
                            System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
                            Printer.printToFile(dateF.format(date) + ": client used a wrong password", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                        }
                            break;
                    case 'r':
                        BashIn.exec("rm storage.txt");
                        BashIn.exec("touch storage.txt");
                    case 'H':
                        toClient.println("I'm fine, thanks");
                        break;
                    default:
                        //also irrelevant
                        System.out.println("hat wohl nich gegeht ¯\\_(ツ)_/¯ ");
                        Printer.printToFile(dateF.format(date) + ": Wie zur hölle bist du hier gelandet??? #nochweirdererflex", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));
                }




                        //Justin wollte iwie ne nachricht idk
                        toClient.println("Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Komplett Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen\n");
                        connected.close();
                        break;

                }

            }
        }


    private static boolean hashCheck (String tHash){
        if(tHash.equals(/*hash aus speicher?*/oriHash)) return true;
        else return false;
    }

}
