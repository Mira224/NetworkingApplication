import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Controller {

    public static void main(String[] args) throws IOException {

        int TCPport = 9999;
        int UDPport = 9998;

        Thread server = new Thread(() -> {
            try {
                new v3Server(TCPport, UDPport,args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();
        boolean o = false;
        Scanner open = new Scanner(System.in);
        do {

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            System.out.print("Do you want to start discovery process? (Y/N): ");
            String option = open.nextLine();

            if (option.equalsIgnoreCase("y")) {
                o = true;
                new v3Client(args);
                break;
            }
            else if(option.equalsIgnoreCase("n")) {
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }else{
                System.out.println("Invalid input.");
            }

        } while (!o);

    }

}