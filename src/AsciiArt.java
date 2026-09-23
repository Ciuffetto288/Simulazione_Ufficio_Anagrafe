
public final class AsciiArt {
    
    private static final String AsciiArtAnagrafe = "  ______   __    __   ______    ______   _______       ______      ________  ________ \r\n" + 
                " /      \\ /  \\  /  | /      \\  /      \\ /       \\    _/      \\_   /        |/        |\r\n" + 
                "/$$$$$$  |$$  \\ $$ |/$$$$$$  |/$$$$$$  |$$$$$$$  |  / $$$$$$   \\  $$$$$$$$/ $$$$$$$$/ \r\n" + 
                "$$ |__$$ |$$$  \\$$ |$$ |__$$ |$$ | _$$/ $$ |__$$ | /$$$ ___$$$  \\ $$ |__    $$ |__    \r\n" + 
                "$$    $$ |$$$$  $$ |$$    $$ |$$ |/    |$$    $$< /$$/ /     $$  |$$    |   $$    |   \r\n" + 
                "$$$$$$$$ |$$ $$ $$ |$$$$$$$$ |$$ |$$$$ |$$$$$$$  |$$ |/$$$$$ |$$ |$$$$$/    $$$$$/    \r\n" + 
                "$$ |  $$ |$$ |$$$$ |$$ |  $$ |$$ \\__$$ |$$ |  $$ |$$ |$$  $$ |$$ |$$ |      $$ |_____ \r\n" + 
                "$$ |  $$ |$$ | $$$ |$$ |  $$ |$$    $$/ $$ |  $$ |$$ |$$  $$  $$/ $$ |      $$       |\r\n" + 
                "$$/   $$/ $$/   $$/ $$/   $$/  $$$$$$/  $$/   $$/ $$  \\$$$$$$$$/  $$/       $$$$$$$$/ \r\n" + 
                "                                                   $$   \\__/   |                      \r\n" + 
                "                                                    $$$    $$$/                       \r\n" + 
                "                                                      $$$$$$/                         ";
                
    private AsciiArt() {
    }

    
    public static void showSplash(ConsoleUtils console, boolean fast) {
        console.clearScreen();
        System.out.println(AnsiColor.paint(AsciiArtAnagrafe, AnsiColor.GREEN));
        System.out.println("Autore: Ciuffetto288");
        System.out.println("Versione: 5.3.1");
        System.out.println("Sistema gestione anagrafe");
        try {
            Thread.sleep(fast ? 300 : 5000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        console.clearScreen();
    }
}
