import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static Date date = new Date();
    public static void main(String[] args) throws Exception {
        while(true) {

            try{
                System.out.println("Starting...");
                // TCP Server starten...
                TCPServer.main(null);
            }
            catch(Exception e){
                System.out.println("Closing...?");
                Printer.printToFile(dateF.format(date) + ": server crashed?", new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true))));;
            }
        }
    }
}
