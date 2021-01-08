import java.io.OutputStream;


public class Main {

    static GpioController gpio = new GpioController();

    public static void main(String[] args) throws Exception {

        System.out.println("Wird gestartet...");
        // TCP Server starten...
        TCPServer.main(null);

        System.out.println("Programm wird beendet...");

    }
}
