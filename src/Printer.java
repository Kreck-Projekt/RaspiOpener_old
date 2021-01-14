import java.io.PrintWriter;

public class Printer {

    public static void printToFile(String text, PrintWriter pw){
        pw.println(text);
        pw.flush();
        pw.close();
    }

}
