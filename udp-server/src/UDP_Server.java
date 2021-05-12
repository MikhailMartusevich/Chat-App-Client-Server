import java.net.*;
import java.io.*;


public class UDP_Server extends Thread  {

    public final static int PORT = 8888;        // ServerSocket port
    private DatagramSocket socket;              // UDP socket
    private boolean running;                    // server status variable
    private byte[] recvBuffer = new byte[256];  // receiving messages buffer
    private byte[] sendBuffer = new byte[256];  // sending messages buffer

    public UDP_Server() throws IOException {
        socket = new DatagramSocket(PORT);
    }

    public void Run() throws IOException {
        running = true;
        System.out.println("Server started");

        while (running) {
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length); // UDP receiving packet

            System.out.println("Waiting for a client message... ");
            socket.receive(recvPacket); // receive data from client

            String recvData = new String(recvPacket.getData()); // convert recieved data to string
            System.out.println("Received from client: " + recvData);

            if (recvData.equals("end")) {
                System.out.println("Server ended");
                running = false;
            }
        }
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        try {
            UDP_Server server = new UDP_Server();
            server.Run();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

}
