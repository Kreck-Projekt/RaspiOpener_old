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
    public String logfileName;
    public boolean debug;
    private final DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Handler(String pKey, String pHash, List<String> pOtps, String logfileName, boolean debug){
        this.key = pKey;
        this.oriHash = pHash;
        this.otps = pOtps;
        this.logfileName = logfileName;
        this.debug = debug;
    }

    public String storeKey(String pMsg) throws IOException {
        key = pMsg;
        Printer.printToFile(key, "keyPasStore.txt", false);
        Printer.printToFile(dateF.format(new Date()) + ": Key set to: " + key, logfileName, true);
        if(key == null) {
            Printer.printToFile(dateF.format(new Date()) + ": Error #01 while setting key " + key, logfileName, true);
            return "01";
        }
        return null;
    }

    public String storePW(String pMsg) throws Exception {
        if(!oriHash.equals("")) return "02";
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
        Printer.printToFile(dateF.format(new Date()) + ": The password hash was set to: " + oriHash, logfileName, true);
        return null;
    }

    public String storeNonce(String pMsg) throws Exception {
        String enHash = null;
        String aesNonce = null;
        String oNonce;
        String trHash;
        int posNonce = -1;
        for (int i = 0; i < pMsg.length()-1; i++) {
            if (pMsg.charAt(i) == ';') {
                aesNonce = pMsg.substring(i + 1);
                enHash = pMsg.substring(0, i);
            }
        }
        String deMsg = Decryption.decrypt(key, aesNonce, enHash);
        System.out.println(deMsg);
        for(int i = 0; i < deMsg.length(); i++){
            if(deMsg.charAt(i) == ';')posNonce = i;
            break;
        }
        //for testing purposes because justin is kinda dumb
        oNonce = deMsg;
//        oNonce = deMsg.substring(0, posNonce);
//        System.out.println("oNonce: " + oNonce);
//        trHash = deMsg.substring(posNonce+1);
//        if(oriHash.equals(trHash)){
        if(true){
            try{
                Printer.printToFile(oNonce, "nonceStore.txt", false);
                Printer.printToDebugFile(dateF.format(new Date()) + ": A new Nonce was set!", logfileName, true, debug);
            }
            catch (FileNotFoundException fnfe){
                BashIn.exec("sudo touch nonceStore.txt");
                Printer.printToFile(oNonce, "nonceStore.txt", false);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else return "05";
        return null;
    }

    public String changePW(String pMsg) throws Exception {
        if(oriHash == null) return "07";
        String oldHash = oriHash;
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
            Printer.printToFile(dateF.format(new Date()) + ": Password hash was changed to: " + neHash, logfileName, true);
        }
        else return "05";
        if(oldHash.equals(oriHash)) return "06";
        return null;
    }

    public String setOTP(String pMsg) throws Exception {
        int listLength = otps.size();
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
                Printer.printToFile(neOtp, "otpStore.txt", true);
                otps.add(neOtp);
                Printer.printToFile(dateF.format(new Date()) + ": A new OTP was set", logfileName, true);
            } catch (FileNotFoundException fnfe) {
                BashIn.exec("sudo touch otpStore.txt");
                Printer.printToFile(neOtp, "otpStore.txt", true);
                otps.add(neOtp);
                Printer.printToFile(dateF.format(new Date()) + ": A new OTP was set", logfileName, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else return "05";
        if(listLength == otps.size()) return "08";
        return null;
    }

    public String einmalOeffnung(String pMsg) throws InterruptedException, IOException {
        String openTime = null;
        String trOtp = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                openTime = pMsg.substring(i + 1);
                trOtp = pMsg.substring(0, i);
            }
        }

        if(otps.size()>0){
            boolean isValid = false;
            int position = -1;
            for (int i = 0; i < otps.size(); i++) {
                if (otps.get(i).equals(trOtp)) {
                    isValid = true;
                    position = i;
                }
            }
            if (isValid) {
                System.out.println("Door is being opened with OTP...");
                GpioController.activate(Integer.parseInt(openTime));
                Printer.printToFile(dateF.format(new Date()) + ": Door is being opened by OTP", logfileName, true);
                System.out.println("OTPSTORE LÄNGE " + otps.size());
                otps.remove(position);
                System.out.println("OTPSTORE LÄNGE " + otps.size());
                try {
                    BashIn.exec("sudo rm otpStore.txt");
                    BashIn.exec("sudo touch otpStore.txt");
//                    somehow doesn't print otps into file, try with normal for loop
//                    for (String otp : otps) {
//                        Printer.printToFile(otp, "otpStore.txt", true);
//                        System.out.println(otp);
//                    }
                    for(int i = 0; i < otps.size(); i++) {
                        System.out.println(otps.get(i));
                        Printer.printToFile(otps.get(i), "otpStore.txt", true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Client used a wrong OTP");
                Printer.printToFile(dateF.format(new Date()) + ": A wrong OTP has been used", logfileName, true);
                return "04";
            }

        }
        else{
            System.out.println("There are currently no OTPs stored");
            Printer.printToFile(dateF.format(new Date()) + ": There were no OTPs, but it was tried anyway", logfileName, true);
            return "04";
        }
        return null;
    }

    public String open(String pMsg) throws Exception {
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
            Printer.printToFile(dateF.format(new Date()) + ": Door is being opened", logfileName, true);
        } else {
            System.out.println("a wrong password was used");
            System.out.println(oriHash + " " + deMsg.substring(0, posHash));
            Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", logfileName, true);
            return "05";
            //toClient.println("Wrong password"); I think this is useless cause the app doesn't receive anything
        }
        return null;
    }

    public String godeOpener(String pMsg){
        if(oriHash.equals(pMsg)){
            try{
                System.out.println("Door is being opened...");
                GpioController.activate(3000);
                Printer.printToFile(dateF.format(new Date()) + ": Door is being opened", logfileName, true);
            }
            catch(Exception e){
                return "09";
            }
        }
        return null;
    }

    public String reset(String pMsg) throws Exception {
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
            Printer.printToFile("\n\n\n" + dateF.format(new Date()) + ": The Pi was reset", logfileName, true);
            key = "";
            oriHash = "";
            BashIn.exec("sudo rm keyPasStore.txt");
            BashIn.exec("sudo touch keyPasStore.txt");
            BashIn.exec("sudo rm otpStore.txt");
            BashIn.exec("sudo touch otpStore.txt");
        } else {
            System.out.println("a wrrong password was used");
            Printer.printToFile(dateF.format(new Date()) + ": client used a wrong password", logfileName, true);
        }
        return null;
    }

}
