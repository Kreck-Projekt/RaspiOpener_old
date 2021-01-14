import java.io.IOException;

public class BashIn {

    // hat noch keine funktion, weil ich das noch nicht zum laufen bekommen hab.

    public static void main(String [] args){

    }

    public static void exec(String cmd) throws IOException {
        Runtime.getRuntime().exec(cmd);
    }

}
