import java.io.OutputStream;


public class Main {
    static GpioController gpio = new GpioController();
    public static void main(String[] args) throws Exception {
        System.out.println("Wird gestartet...");
        // TCP Server starten...
        TCPServer.main(null);




        /*
        try {
            gpio.activate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // pins anschalten
         */



        //PwdChange.main(null);
        //Process p = Runtime.getRuntime().exec(new String[]{"bash","ls /home/pi"});
        Runtime rt = Runtime.getRuntime();
        Process processes = Runtime.getRuntime().exec("echo rofl");
        OutputStream os = processes.getOutputStream();
        System.out.println("penis");
        //System.out.println(p);
    }
}
