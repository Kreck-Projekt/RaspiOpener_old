import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static StringWriter sw = new StringWriter();
    private static PrintWriter pw = new PrintWriter(sw);
    private static DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static void main(String[] args) throws Exception {

        try{
            System.out.println("Starting...");
            // TCP Server starten...
            TCPServer.run();
        }
        catch(Exception e){
            e.printStackTrace(pw);
            System.out.println("Closing...?");
            Printer.printToFile(dateF.format(new Date()) + ": server crashed?" + sw.toString(), new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));

        }

    }
}
