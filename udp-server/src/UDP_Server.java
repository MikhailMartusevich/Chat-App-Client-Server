import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class UDP_Server extends Thread  {

    private static class SERVER_COMMANDS {  // structure of server commands
        final String SERVER_END = "/end";
        final String REG_USER = "/reg";
        final String LOG_USER = "/login";
        final String LOGOUT_USER = "/logout";
    }

    private static class SERVER_USER {  // structure of server clients
        String userName;
        InetAddress userAddress;
        boolean isOnline = false;

        public SERVER_USER(String _name, InetAddress _inetaddr) {
            userName = _name;
            userAddress = _inetaddr;
            isOnline = false;
        }
    }

    public final static int         SERVER_PORT = 8888;         // ServerSocket port
    private final DatagramSocket    SERVER_SOCKET;              // UDP socket
    private boolean isRunning;                                  // server status variable

    private byte[]  RECEIVE_BUFFER  = new byte[256];  // receiving messages buffer
    //private byte[]  SEND_BUFFER     = new byte[256];  // sending messages buffer


    public UDP_Server() throws IOException {
        SERVER_SOCKET = new DatagramSocket(SERVER_PORT);
    }

    public void Run() throws IOException {
        SERVER_COMMANDS commands = new SERVER_COMMANDS();
        ArrayList<SERVER_USER> users = new ArrayList<SERVER_USER>();

        isRunning = true;
        System.out.println("Server started");


        while (isRunning) {
            DatagramPacket RECEIVED_PACKET = new DatagramPacket(RECEIVE_BUFFER, RECEIVE_BUFFER.length); // UDP receiving packet

            System.out.println("Waiting for a client message... ");
            SERVER_SOCKET.receive(RECEIVED_PACKET); // receive data from client

            String RECEIVED_DATA = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength()); // convert received data to string
//            System.out.println("Received from client: " + RECEIVED_DATA);


            /* Server received registration command (/reg)*/

            if (RECEIVED_DATA.equals(commands.REG_USER)) { // if received register command
                RECEIVE_BUFFER = new byte[256]; //clearing for a new message
                boolean exist = false;
                InetAddress regAddress = RECEIVED_PACKET.getAddress(); // save address of client

                do {
                    SERVER_SOCKET.receive(RECEIVED_PACKET);
                } while (!RECEIVED_PACKET.getAddress().equals(regAddress)); // waiting to receive username

                String regUser = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength());

                for (int i = 0; i < users.size(); i++)
                    if (regUser.equals(users.get(i).userName)) { // check all users for equal username
                        exist = true;
                        // send error to client
                        break;
                    }

                if (!exist) {
                    users.add(new SERVER_USER(regUser,RECEIVED_PACKET.getAddress())); // adding new client to userlist
                    // send registration success to client
                    System.out.println("New user registered: " + regUser);
                }
            }


            /* Server received login command (/login) */

            else if (RECEIVED_DATA.equals(commands.LOG_USER)) {
                RECEIVE_BUFFER = new byte[256]; //clearing for a new message
                boolean exist = false;
                InetAddress loginAddress = RECEIVED_PACKET.getAddress();

                do {
                    SERVER_SOCKET.receive(RECEIVED_PACKET);
                } while (!RECEIVED_PACKET.getAddress().equals(loginAddress)); // waiting to receive username


                String loginUser = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength());

                for (int i = 0; i < users.size(); i++) {
                    if (loginUser.equals(users.get(i).userName) && !users.get(i).isOnline) // check all users for equal username
                        exist = true;

                    if (exist) {
                        users.get(i).userAddress = RECEIVED_PACKET.getAddress();
                        users.get(i).isOnline = true;
                        // send login success to client
                        System.out.println("User logged in: " + loginUser);
                        break;
                    }
                }

                if (!exist) {
                    // send error to client: user is online
                }
            }

            /* Server received user logout command (/logout) */

            else if (RECEIVED_DATA.equals(commands.LOGOUT_USER)) {
                continue;
            }

            /* Server received end of work command (/end) */
            else if (RECEIVED_DATA.equals(commands.SERVER_END)) {
                System.out.println("Server ended");
                // send to all users ending message
                isRunning = false;
            }

            /* Server received user's message */
            else {
                System.out.println("Received from client: " + RECEIVED_DATA);
                // send message to all users
            }

            RECEIVE_BUFFER = new byte[256]; //clearing for a new message
        }
        SERVER_SOCKET.close();
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
