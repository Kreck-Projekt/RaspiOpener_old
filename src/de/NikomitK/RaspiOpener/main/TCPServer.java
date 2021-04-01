package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.*;

import javax.crypto.AEADBadTagException;
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
    static String oriHash = "";  //original hash, just saved here for testing purposes
    static List<String> otps;

    public static void run(String logfileName, boolean debug) throws Exception {
        File keyPasStore = new File("keyPasStore.txt");
        File otpStore = new File("otpStore.txt");
        DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Scanner kpsc;
        Scanner otpscan;
        try {
            kpsc = new Scanner(keyPasStore);
        }
        catch (Exception e){
            BashIn.exec("sudo touch keyPasStore.txt");
            kpsc = new Scanner(keyPasStore);
        }
        try{
            otpscan = new Scanner(otpStore);
        }
        catch (Exception e){
            BashIn.exec("sudo touch otpStore.txt");
            otpscan = new Scanner(otpStore);
        }
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
        Handler handler = new Handler(key, oriHash, otps, logfileName, debug);
        handler.key = key;
        handler.oriHash = oriHash;

        String fromclient;

        // first startup
        boolean fsu = true;

        ServerSocket Server = new ServerSocket(5000);

        System.out.println("TCPServer waiting for client on port 5000 ");
        Printer.printToFile(dateF.format(new Date()) + ": Server starts", logfileName, true);
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
                System.out.println("Received: " + fromclient);
                try {
                    if (fromclient.charAt(1) != ':' && fromclient.equals("null")) {
                        try {
                            toClient.println("Invalid connection\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        connected.close();
                        continue;
                    } //checks if the sent message is a command#
                } catch (Exception e) {
                    try {
                        toClient.println("Invalid connection\n");
                    } catch (Exception ex) {
                        e.printStackTrace();
                    }
                    connected.close();
                    continue;
                }
                if (fromclient.charAt(0) != 'H')
                    Printer.printToFile(dateF.format(new Date()) + ": Client at: " + connected.getInetAddress() + " sent " + fromclient.charAt(0) + " command", logfileName, true);
                String param;
                try {
                    param = fromclient.substring(2);
                } catch (Exception e) {
                    e.printStackTrace();
                    connected.close();
                    break;
                }

                String worked = null;
                try {
                    switch (fromclient.charAt(0)) {
                        case 'n': //irrelevant
                            break;

                        case 'k': //storeKey done
                            // Command syntax: "k:<key>"
                            if (((key == null || key.equals("")) && param.length() == 32) && secured)
                                worked = handler.storeKey(param);
                            key = handler.key;
                            break;

                        case 'p': //storePW done
                            // Command syntax: "p:(<hash>);<nonce>"
                            worked = handler.storePW(param);
                            oriHash = handler.oriHash;
                            break;

                        case 'c': //changePW done
                            // Command syntax: "c:(<oldHash>;<newHash>);<nonce>"
                            worked = handler.changePW(param);
                            break;

                        case 's': // setOTP done
                            // Command syntax "s:(<otp>;<hash>);<nonce>>"
                            worked = handler.setOTP(param);
                            otps = handler.otps;
                            break;

                        case 'e': // einmalöffnung done
                            // Command syntax: "e:<otp>;<time>"
                            worked = handler.einmalOeffnung(param);
                            otps = handler.otps;
                            break;

                        case 'o': // Open  done
                            // Command syntax: "o:(<hash>;<time>);<nonce>"
                            worked = handler.open(param);
                            break;

                        case 'r': //reset
                            // Command syntax: "r:(<hash>);<nonce>"
                            worked = handler.reset(param);
                            key = handler.key;
                            oriHash = handler.oriHash;
                            break;

                        case 'H': // "how are you", get's called from alivekeeper, never from user
                            toClient.println("I'm fine, thanks");
                            break;

                        default:
                            //also irrelevant
                            System.out.println("What happened here?");
                            Printer.printToFile(dateF.format(new Date()) + ": This wasn't supposed to happen :/ ERROR #10", logfileName, true);
                            Printer.printToFile(dateF.format(new Date()) + ": The whole message was: " + fromclient, logfileName, true);
                            toClient.println("10");
                            toClient.println("What do you want from this poor Server? \uD83E\uDD7A");
                            break;
                    }
                }
                catch(AEADBadTagException bte){
                    bte.printStackTrace();
                    worked = "03";
                }
                catch (Exception exc){
                    exc.printStackTrace();
                    worked = null;
                }

                if(worked != null) {
                    toClient.println(worked + "EOS");
                    connected.close();
                }
            }


            catch(Exception e){
                e.printStackTrace();
            }

            }

        }


}
