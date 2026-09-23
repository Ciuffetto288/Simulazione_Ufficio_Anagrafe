import java.util.Arrays;
import java.util.Scanner;

public final class Main {
    private Main() {
    }
    
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--self-test")) {
            SelfTest.run();
            return;
        }

        boolean fastSplash = Arrays.asList(args).contains("--fast");

        
        try (Scanner scanner = new Scanner(System.in)) {
            new Menu(scanner).start(fastSplash);
        }
    }
}
