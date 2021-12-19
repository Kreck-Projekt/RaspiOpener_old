package de.NikomitK.RaspiOpener.main;

import de.NikomitK.RaspiOpener.handler.Error;
import de.NikomitK.RaspiOpener.handler.Handler;
import lombok.experimental.UtilityClass;

import javax.crypto.AEADBadTagException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@UtilityClass
class TCPServer {

    public static void run() throws Exception {

        ServerSocket server = new ServerSocket(5000);

        Main.getLogger().log("Server Starting on port 5000");
        while (true) {

            Main.getLogger().log("Waiting for a connection...");
            Socket connected = server.accept();
            connected.setSoTimeout(3000);
            Main.getLogger().log("Client at " + " " + connected.getInetAddress() + ":" + connected.getPort() + " connected ");

            BufferedReader fromClient = new BufferedReader(new InputStreamReader(connected.getInputStream()));

            PrintWriter toClient = new PrintWriter(connected.getOutputStream(), true);

            toClient.println("Connected");
            // receive from app
            String msgFromClient = fromClient.readLine();
            Main.getLogger().log("Received: " + msgFromClient);
            if (!isCommand(msgFromClient)) { //checks if the sent message is a command
                try {
                    toClient.println("Invalid connection\n");
                    Main.getLogger().warn("Invalid Message");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connected.close();
                continue;
            }
            Main.getLogger().log("Client at: " + connected.getInetAddress() + " sent " + msgFromClient.charAt(0) + " command");

            String param = msgFromClient.substring(2);
            Error worked = null;
            try {
                switch (msgFromClient.charAt(0)) {
                    case 'n': //storeNonce done
                        // Command syntax: "n:(<nonce>;<hash>);nonce
                        worked = Handler.storeNonce(param);
                        break;

                    case 'k': //storeKey done
                        // Command syntax: "k:<key>"
                        if (((Main.getStorage().getKey() == null || Main.getStorage().getKey().equals("")) && param.length() == 32))
                            worked = Handler.storeKey(param);
                        break;

                    case 'p': //storePW done
                        // Command syntax: "p:(<hash>);<nonce>"
                        if (Main.getStorage().getHash() == null || Main.getStorage().getHash().equals("")) {
                            worked = Handler.storePW(param);
                        }
                        break;

                    case 'c': //changePW done
                        // Command syntax: "c:(<oldHash>;<newHash>);<nonce>"
                        worked = Handler.changePW(param);
                        break;

                    case 's': // setOTP done
                        // Command syntax "s:(<hash>;<otp>);<nonce>>"
                        worked = Handler.setOTP(param);
                        break;

                    case 'e': // einmalöffnung done
                        // Command syntax: "e:<otp>;<time>"
                        worked = Handler.einmalOeffnung(param);
                        break;

                    case 'o': // Open  done
                        // Command syntax: "o:(<hash>;<time>);<nonce>"
                        worked = Handler.open(param);
                        break;

                    case 'g': // godeOpener? done
                        // command syntax: "g:<hash><otp>"
                        /*
                         * this case is for usage with the CodeOpener extension
                         * you can use a numpad/keyboard to open your door,
                         * works for the normal password and OTPs
                         */
                        worked = Handler.godeOpener(param);
                        break;

                    case 'r': //reset
                        // Command syntax: "r:(<hash>);<nonce>"
                        worked = Handler.reset(param);
                        break;

                    case 'H': // "how are you", get's called from alivekeeper, never from user
                        // alivekeeper was kinda abandoned, so idk if this will ever be used again, maybe remove later
                        toClient.println("I'm fine, thanks");
                        break;

                    default:
                        //also irrelevant
                        System.out.println("What happened here?");
                        Main.getLogger().error("This wasn't supposed to happen :/ ERROR #10");
                        Main.getLogger().error("The whole message was: " + msgFromClient);
                        toClient.println("10");
                        toClient.println("What do you want from this poor Server? \uD83E\uDD7A");
                        break;
                }
            } catch (AEADBadTagException bte) {
                bte.printStackTrace();
                worked = Error.KEY_MISMATCH;
            }

            if (worked != null) {
                toClient.println(worked + "EOS");
                connected.close();
            }

        }

    }

    private static boolean isCommand(String msg) {
        if(msg == null) return false;
        return msg.charAt(1) == ':' && !msg.equals("null") && msg.length() > 5;
    }
}
