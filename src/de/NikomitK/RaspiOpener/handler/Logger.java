package de.NikomitK.RaspiOpener.handler;

import de.NikomitK.RaspiOpener.main.Main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private final BufferedOutputStream bOutputStream;
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public Logger(File logFile) throws IOException {
        if(!logFile.exists()){
            logFile.createNewFile();
        }
        bOutputStream = new BufferedOutputStream(new FileOutputStream(logFile, true));
    }

    public void log(String msg) {
        try{
            msg = dateFormat.format(new Date()) + " [Info] " + msg + "\n";
            print(msg);
            bOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void warn(String msg){
        try{
            msg = dateFormat.format(new Date()) + " [Warn] " + msg + "\n";
            print(msg);
            bOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void error(String msg){
        try{
            msg = dateFormat.format(new Date()) + " [Error] " + msg + "\n";
            print(msg);
            bOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(String msg){
        if(!Main.isDebug()){
            return;
        }
        else{
            try{
                msg = dateFormat.format(new Date()) + " [Debug] " + msg + "\n";
                print(msg);
                bOutputStream.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void print(String msg) throws IOException {
        bOutputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(msg);
    }
}
