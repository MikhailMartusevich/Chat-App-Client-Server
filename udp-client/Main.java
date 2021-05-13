import java.io.IOException;
import java.net.*;
import java.util.Scanner;


public class Main extends Thread {

    public static void main(String[] args) throws IOException {

        final int port = 8888;
        String text = " ";
        Scanner scanner = new Scanner(System.in);
        byte[] sendingDataBuffer = new byte[256];
        DatagramPacket packet;

        try {
            DatagramSocket socket = new DatagramSocket(port);
            InetAddress ip =  //InetAddress.getLocalHost();
            InetAddress.getByName("DESKTOP-AU83J05");

            new SendThread(socket, port, ip).start();
            new ReceiveThread(socket).start();
        }


        catch (SocketException exp) {
            exp.printStackTrace();
        }

        catch (UnknownHostException exp) {
            exp.printStackTrace();
        }
    }
}
