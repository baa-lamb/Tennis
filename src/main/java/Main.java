
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("c/exit");
        Server s = new Server();
        while (true) {
            Scanner in = new Scanner(System.in);
            String answer = in.next();
            if (answer.equals("c")) {
                s.newGame();
            }
            else if (answer.equals("exit")) {
                s.close();
                break;
            }
        }
        System.exit(0);
    }
}
