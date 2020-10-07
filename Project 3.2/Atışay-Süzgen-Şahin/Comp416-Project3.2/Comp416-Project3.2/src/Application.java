import java.io.FileNotFoundException;
import java.util.Scanner;

/***
 * This is the main function where the simulator instantiated.
 * It takes input from the user to decide on the period duration.
 */
public class Application {
    public static void main(String args[]) throws FileNotFoundException {
        System.out.println("Enter the period duration in seconds: ");
        Scanner scanner = new Scanner(System. in);
        int period = Integer.parseInt(scanner. nextLine());
        ModivSim.getInstance(period).run();
    }
}
