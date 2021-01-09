import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class TCPServer {
    static String key;
    static String nonce;
    static String oriHash = "penis";  //original hash, just saved here for testing purposes
    static String tHash; //transmitted hash
    public static void main(String args[]) throws Exception {
        File myObj = new File("storage.txt");
        Scanner sc = new Scanner(myObj);
        key = sc.nextLine();
        oriHash = sc.nextLine();
        String fromclient;

        ServerSocket Server = new ServerSocket(5000);

        System.out.println("TCPServer waiting for client on port 5000");

        while (true) {
            Socket connected = Server.accept();
            System.out.println(" THE CLIENT" + " " + connected.getInetAddress()
                    + ":" + connected.getPort() + " IS CONNECTED ");


            BufferedReader inFromUser = new BufferedReader(
                    new InputStreamReader(System.in));

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connected.getInputStream()));

            PrintWriter outToClient = new PrintWriter(connected.getOutputStream(), true);
            // shababs botten, grüne augen braune locken
            outToClient.println("Connected");
            // System.out.println("Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Komplett Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen\n")
            // this loop is probably useless, since it will break after every time it happens, but i reused old server code
            // that i've written a while back and just changed it because i'm lazy?
            while (true) {

                // receive from app
                fromclient = inFromClient.readLine();
                System.out.println("RECIEVED: " + fromclient);
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
                        System.out.println("Nonce angekommen, du keks");
                        break;
                    case 'k':
                        key = param;
                        Printer.printToFile(key, new PrintWriter(new BufferedWriter(new FileWriter("storage.txt"))));
                        System.out.println(key + " gespeichert");
                        break;
                    case 'p':
                        if(oriHash == null) {
                            for (int i = 0; i < param.length(); i++) {
                                if (param.charAt(i) == ';') {
                                    nonce = param.substring(i + 1);
                                    param = param.substring(0, i);
                                }
                            }
                            String s = Decryption.decrypt(key, nonce, param);
                            oriHash = s;
                            Printer.printToFile(oriHash, new PrintWriter(new BufferedWriter(new FileWriter("storage.txt", true))));
                            System.out.println(s);
                            break;
                        }
                        else{

                        }
                    case 'a': // a für "password action" aka halts maul justin und formulier gescheit was du sagen willst
                        System.out.println("PaSsWoRd AcTiOn");
                        System.out.println("Junge sag doch einfach, dass das als öffnen gemeint war");
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

                        System.out.println("Hash: " + dcrMsg.substring(0, posPas));
                        System.out.println("Param: " + dcrMsg.substring(posPas+1));
                        if (hashCheck(dcrMsg.substring(0, posPas))) {
                            System.out.println("Türe wird geöffnet...");
                            GpioController.activate(Integer.valueOf(dcrMsg.substring(posPas+1)));
                        }
                        else System.out.println("ding dong, your password is wrong\n̿̿ ̿̿ ̿̿ ̿'̿'\\̵͇̿̿\\з= ( ▀ ͜͞ʖ▀) =ε/̵͇̿̿/’̿’̿ ̿ ̿̿ ̿̿ ̿̿");
                            break;
                    case 'H':
                        outToClient.println("I'm fine, thanks");
                        break;
                    default:
                        System.out.println("hat wohl nich gegeht ¯\\_(ツ)_/¯ ");
                }




                        //Justin wollte iwie ne nachricht idk
                        outToClient.println("Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Komplett Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Ey (K-K-Kingsake) Ey, Pasha, ey, ja, ey (YungGlizzy) Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Prada Çanta voller Haze Kafa, outta Race Braune Locken, grüne Augen Shababs sind am Botten, no face, no case, ey Halbe Kiste im Toyota gebunkert Einundsechzig, keiner sober, nur Kundschaft Neue Ringe, neue Eyes Einundsechzig, gerade heiß Shawty, sag' mir, wie du heißt Hab' vergessen, tut mir leid An Sarrazin will ich verdienen, bin in Berlin Sein Sohn holt jede Woche Tilidin Mehringdamm, Saka-Wasser oder Lean Müsteris schreiben mir: „Komm' vorbei“ Und alle paar Monate flieg' ich Türkei Ich ess' einen Adana oder auch zwei Abi hat Katana an seinem Bein und Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen Shababs botten, grüne Augen, braune Locken Tn's rocken, halbe Kiste, wenn wir shoppen\n");
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
