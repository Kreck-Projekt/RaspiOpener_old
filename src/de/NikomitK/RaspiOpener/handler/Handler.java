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
    private DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Handler(String pKey, String pHash, List<String> pOtps){
        this.key = pKey;
        this.oriHash = pHash;
        this.otps = pOtps;
    }

    public boolean storeKey(String pMsg) throws IOException {
        key = pMsg;
        Printer.printToFile(key, "storage.txt", false);
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
        Printer.printToFile(oriHash, "storage.txt", true);
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
            Printer.printToFile(key + "\n" + neHash, "storage.txt", false);
            Printer.printToFile(dateF.format(new Date()) + ": Password hash was changed to: " + neHash, "log.txt", true);
        }
        return true;
    }

    public boolean setOTP(String pMsg) throws Exception {
        boolean firstSem = false;
        int posOtp = -1;
        String nonce = null;
        String enMsg = null;
        String deMsg;
        String neOtp;
        String trHash;
        for (int i = 0; i < pMsg.length(); i++) {
            if (!firstSem && pMsg.charAt(i) == ';') firstSem = true;
            else if (firstSem && pMsg.charAt(i) == ';') {
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
        neOtp = deMsg.substring(0, posOtp-1);
        trHash = deMsg.substring(posOtp);
        if(oriHash.equals(trHash)) {
            try {
                Printer.printToFile(neOtp + "\n", "otpStore.txt", true);
                otps.add(neOtp);
            } catch (FileNotFoundException fnfe) {
                BashIn.exec("sudo touch otpStore.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean einmalOeffnung(String pMsg){

        return true;
    }

    public boolean open(String pMsg){

        return true;
    }

    public boolean reset(String pMsg){

        return true;
    }

}
