package de.NikomitK.RaspiOpener.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Handler {
    public String key;
    public String oriHash;
    public List<String> otps;
    private final DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Handler(String pKey, String pHash, List<String> pOtps){
        this.key = pKey;
        this.oriHash = pHash;
        this.otps = pOtps;
    }

    public boolean storeKey(String pMsg) throws IOException {
        key = pMsg;
        Printer.printToFile(key, "keyPasStore.txt", false);
        Printer.printToFile(dateF.format(new Date()) + ": Key set to: " + key, "log.txt", true);
        return true;
    }

    public boolean storePW(String pMsg) throws Exception {
        String enHash = null;
        String nonce = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enHash = pMsg.substring(0, i);
            }
        }
        oriHash = Decryption.decrypt(key, nonce, enHash);
        Printer.printToFile(oriHash, "keyPasStore.txt", true);
        Printer.printToFile(dateF.format(new Date()) + ": The password hash was set to: " + oriHash, "log.txt", true);
        return true;
    }

    public boolean changePW(String pMsg) throws Exception {
        String nonce = null;
        String enHashes = null;
        String trHash = null;
        String neHash = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enHashes = pMsg.substring(0, i);
            }
        }
        String deHashes = Decryption.decrypt(key, nonce, enHashes);
        for (int i = 0; i < deHashes.length(); i++) {
            if (deHashes.charAt(i) == ';') {
                neHash = deHashes.substring(i + 1);
                trHash = deHashes.substring(0, i);
            }
        }
        if(trHash.equals(oriHash)) {
            oriHash = neHash;
            Printer.printToFile(key + "\n" + neHash, "keyPasStore.txt", false);
            Printer.printToFile(dateF.format(new Date()) + ": Password hash was changed to: " + neHash, "log.txt", true);
        }
        return true;
    }

    public boolean setOTP(String pMsg) throws Exception {
        int posOtp = -1;
        String nonce = null;
        String enMsg = null;
        String deMsg;
        String neOtp;
        String trHash;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }
        deMsg = Decryption.decrypt(key, nonce, enMsg);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posOtp = i;
                break;
            }
        }
        neOtp = deMsg.substring(posOtp+1);
        trHash = deMsg.substring(0, posOtp);
        if(oriHash.equals(trHash)) {
            try {
                Printer.printToFile(neOtp + "\n", "otpStore.txt", true);
                otps.add(neOtp);
                Printer.printToFile(dateF.format(new Date()) + ": A new OTP was set", "log.txt", true);
            } catch (FileNotFoundException fnfe) {
                BashIn.exec("sudo touch otpStore.txt");
                Printer.printToFile(neOtp + "\n", "otpStore.txt", true);
                otps.add(neOtp);
                Printer.printToFile(dateF.format(new Date()) + ": A new OTP was set", "log.txt", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean einmalOeffnung(String pMsg) throws InterruptedException, IOException {
        String openTime = null;
        String trOtp = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                openTime = pMsg.substring(i + 1);
                trOtp = pMsg.substring(0, i);
            }
        }
        for (int i = 0; i<otps.size(); i++) {
            if(otps.get(i).equals(trOtp)){
                System.out.println("Door is being opened with OTP...");
                GpioController.activate(Integer.parseInt(openTime));
                Printer.printToFile(dateF.format(new Date()) + ": Door is being opened by OTP", "log.txt", true);
                otps.remove(i);
                try {
                    BashIn.exec("sudo rm otpStore.txt");
                    BashIn.exec("sudo touch otpStore.txt");
                    for (String otp : otps) Printer.printToFile(otp, "otpStore.txt", true);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Client used a wrong OTP");
                Printer.printToFile(dateF.format(new Date()) + ": A wrong OTP has been used", "log.txt", true);
            }
        }
        return true;
    }

    public boolean open(String pMsg) throws Exception {
        int posHash = -1;
        String nonce = null;
        String enMsg = null;
        String deMsg;
        for (int i = 0; i < pMsg.length(); i++) {
            if ( pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }
        deMsg = Decryption.decrypt(key, nonce, enMsg);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posHash = i;
                break;
            }
        }
        if (oriHash.equals(deMsg.substring(0, posHash))) {
            System.out.println("Door is being opened...");
            GpioController.activate(Integer.parseInt(deMsg.substring(posHash + 1)));
            Printer.printToFile(dateF.format(new Date()) + ": Door is being opened", "log.txt", true);
        } else {
            System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
            Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", "log.txt", true);
            //toClient.println("Wrong password"); I think this is useless cause the app doesn't receive anything
        }
        return true;
    }

    public boolean reset(String pMsg) throws Exception {
        String nonce = null;
        String enMsg = null;
        String deMsg;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }
        deMsg = Decryption.decrypt(key, nonce, enMsg);
        if (oriHash.equals(deMsg)) {
            System.out.println("Pi is getting reset...\n");
            Printer.printToFile("\n\n\n" + dateF.format(new Date()) + ": The Pi was reset", "log.txt", true);
            key = "";
            oriHash = "";
            BashIn.exec("sudo rm keyPasStore.txt");
            BashIn.exec("sudo touch keyPasStore.txt");
            BashIn.exec("sudo rm otpStore.txt");
            BashIn.exec("sudo touch otpStore.txt");
        } else {
            System.out.println("ding dong, your password is wrong\n¯\\_(ツ)_/¯");
            Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", "log.txt", true);
        }
        return true;
    }

}
