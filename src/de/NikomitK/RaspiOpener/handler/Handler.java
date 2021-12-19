package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;
import lombok.experimental.UtilityClass;


@UtilityClass
public class Handler {

    public static Error storeKey(String pMsg){
        Main.getStorage().setKey(pMsg);
        Main.getStorage().save();
        Main.getLogger().log("New key set");
        Main.getLogger().debug("New key is: " + pMsg);
        if (Main.getStorage().getKey() == null) {
            Main.getLogger().warn("Key couldn't get set");
            return Error.KEY_NOT_SAVED;
        }
        return Error.OK;
    }

    public static Error storePW(String pMsg) throws Exception {
        if (Main.getStorage().getHash() != null) return Error.PASSWORD_EXISTS;
        String enHash = null;
        String nonce = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enHash = pMsg.substring(0, i);
            }
        }
        Main.getStorage().setHash(Decryption.decrypt(Main.getStorage().getKey(), nonce, enHash));
        Main.getStorage().save();
        Main.getLogger().log("Password hash changed");
        Main.getLogger().debug("New password hash is: " + Main.getStorage().getHash());
        return Error.OK;
    }

    public static Error storeNonce(String pMsg) throws Exception {
        String enHash = null;
        String aesNonce = null;
        String oNonce;
        String trHash; // don't remove
        int posNonce = -1;
        for (int i = 0; i < pMsg.length() - 1; i++) {
            if (pMsg.charAt(i) == ';') {
                aesNonce = pMsg.substring(i + 1);
                enHash = pMsg.substring(0, i);
            }
        }
        String deMsg = Decryption.decrypt(Main.getStorage().getKey(), aesNonce, enHash);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posNonce = i;
                break;
            }
        }
        // for testing purposes because justin is kinda dumb
        // a few weeks later, I have no clue what the hell this was about, but I know it's still not fixed
        // a few months later, I think he failed with the command syntax so the code that should work didn't.
        // fixed in the update that has been ready for months now but still isn't released
        oNonce = deMsg;
//        oNonce = deMsg.substring(0, posNonce);
//        System.out.println("oNonce: " + oNonce);
//        trHash = deMsg.substring(posNonce+1);
//        if(oriHash.equals(trHash)){
        if (true) {
            try {
                Main.getStorage().setNonce(oNonce);
                Main.getStorage().save();
                Main.getLogger().log("A new Nonce was set");
                Main.getLogger().debug("New nonce is: " + oNonce);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else return Error.PASSWORD_MISMATCH;
        return Error.OK;
    }

    public static Error changePW(String pMsg) throws Exception {
        if (Main.getStorage().getHash() == null) return Error.NO_PASSWORD_TO_REPLACE;
        String oldHash = Main.getStorage().getHash();
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
        String deHashes = Decryption.decrypt(Main.getStorage().getKey(), nonce, enHashes);
        for (int i = 0; i < deHashes.length(); i++) {
            if (deHashes.charAt(i) == ';') {
                neHash = deHashes.substring(i + 1);
                trHash = deHashes.substring(0, i);
            }
        }
        assert trHash != null;
        if (trHash.equals(Main.getStorage().getHash())) {
            Main.getStorage().setHash(neHash);
            Main.getStorage().save();
            Main.getLogger().log("Password hash was changed");
            Main.getLogger().debug("New hash is: " + neHash);
        } else return Error.PASSWORD_MISMATCH;
        if (oldHash.equals(Main.getStorage().getHash())) return Error.PASSWORD_NOT_SAVED;
        return Error.OK;
    }

    public static Error setOTP(String pMsg) throws Exception {
        int listLength = Main.getStorage().getOtps().size();
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
        deMsg = Decryption.decrypt(Main.getStorage().getKey(), nonce, enMsg);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posOtp = i;
                break;
            }
        }
        neOtp = deMsg.substring(posOtp + 1);
        trHash = deMsg.substring(0, posOtp);
        if (Main.getStorage().getHash().equals(trHash)) {
            try {
                Main.getStorage().getOtps().add(neOtp);
                Main.getStorage().save();
                Main.getLogger().log("A new OTP was added");
                Main.getLogger().debug("The new OTP is: " + neOtp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else return Error.PASSWORD_MISMATCH;
        if (listLength == Main.getStorage().getOtps().size()) return Error.OTP_NOT_SAVED;
        return Error.OK;
    }

    public static Error einmalOeffnung(String pMsg) throws InterruptedException{
        String openTime = null;
        String trOtp = null;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                openTime = pMsg.substring(i + 1);
                trOtp = pMsg.substring(0, i);
            }
        }

        if (Main.getStorage().getOtps().size() > 0) {
            boolean isValid = false;
            int position = -1;
            for (int i = 0; i < Main.getStorage().getOtps().size(); i++) {
                if (Main.getStorage().getOtps().get(i).equals(trOtp)) {
                    isValid = true;
                    position = i;
                }
            }
            if (isValid) {
                GpioHandler.activate(Integer.parseInt(openTime));
                Main.getLogger().log("Door is being opened by an OTP");
                Main.getLogger().debug("The used OTP is: " + trOtp);
                Main.getStorage().getOtps().remove(position);
            } else {
                Main.getLogger().log("A wrong OTP was sent");
                return Error.OTP_NOT_EXISTING;
            }

        } else {
            Main.getLogger().log("There are no OTPs stored, but it was tried anyway");
            return Error.OTP_NOT_EXISTING;
        }
        return Error.OK;
    }

    public static Error open(String pMsg) throws Exception {
        int posHash = -1;
        String nonce = null;
        String enMsg = null;
        String deMsg;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }
        deMsg = Decryption.decrypt(Main.getStorage().getKey(), nonce, enMsg);
        for (int i = 0; i < deMsg.length(); i++) {
            if (deMsg.charAt(i) == ';') {
                posHash = i;
                break;
            }
        }
        if (Main.getStorage().getHash().equals(deMsg.substring(0, posHash))) {
            GpioHandler.activate(Integer.parseInt(deMsg.substring(posHash + 1)));
            Main.getLogger().log("Door is being opened...");
        } else {
            Main.getLogger().log("A wrong password was used"); // this shouldn't be possible, because the encryption is flawed
            return Error.PASSWORD_MISMATCH;
        }
        return Error.OK;
    }

    public static Error godeOpener(String pMsg) {
        int posSem = 0;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                posSem = i;
            }
        }

        if (Main.getStorage().getHash().equals(pMsg.substring(0, posSem))) {
            try {
                GpioHandler.activate(Main.openTime);
                Main.getLogger().log("Door is being opened by keypad");
            } catch (Exception e) {
                return Error.INTERNAL_SERVER_ERROR;
            }
        } else {
            try {
                einmalOeffnung(pMsg.substring(posSem + 1) + ";3000");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Error.OK;
    }

    public static Error reset(String pMsg) throws Exception {
        String nonce = null;
        String enMsg = null;
        String deMsg;
        for (int i = 0; i < pMsg.length(); i++) {
            if (pMsg.charAt(i) == ';') {
                nonce = pMsg.substring(i + 1);
                enMsg = pMsg.substring(0, i);
            }
        }
        deMsg = Decryption.decrypt(Main.getStorage().getKey(), nonce, enMsg);
        if (Main.getStorage().getHash().equals(deMsg)) {
            Main.getLogger().log("Pi is getting reset...");
            Main.resetStorage();
        } else {
            Main.getLogger().log("Client tried resetting with a wrong password"); // again, this shouldn't happen due to the nature of the encryption
        }
        return Error.OK;
    }

}
