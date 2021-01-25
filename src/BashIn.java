import java.io.IOException;

public class BashIn {

    // shorter command for executing linux shell commands
    public static void exec(String cmd) throws IOException {
        Runtime.getRuntime().exec(cmd);
    }

}
