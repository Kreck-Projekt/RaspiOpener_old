import java.io.OutputStream;


public class Main {

    static GpioController gpio = new GpioController();

    public static void main(String[] args) throws Exception {

        System.out.println("Starting...");
        // TCP Server starten...
        TCPServer.main(null);

        System.out.println("Closing...?");

    }
}
