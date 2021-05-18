import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
        boolean isOnline;

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
    private byte[]  SEND_BUFFER     = new byte[256];  // sending messages buffer


    public UDP_Server() throws IOException {
        SERVER_SOCKET = new DatagramSocket(SERVER_PORT);
    }

    public void Run() throws IOException {
        SERVER_COMMANDS commands = new SERVER_COMMANDS();
        ArrayList<SERVER_USER> users = new ArrayList<>();

        isRunning = true;
        System.out.println("Server started");
        System.out.println("Waiting for a client message... ");

        while (isRunning) {
            DatagramPacket RECEIVED_PACKET = new DatagramPacket(RECEIVE_BUFFER, RECEIVE_BUFFER.length); // UDP receiving packet
            DatagramPacket SEND_PACKET;


            SERVER_SOCKET.receive(RECEIVED_PACKET); // receive data from client

            String RECEIVED_DATA = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength()); // convert received data to string


            /* Server received registration command (/reg)*/

            if (RECEIVED_DATA.equals(commands.REG_USER)) { // if received register command
                RECEIVE_BUFFER = new byte[256]; //clearing for a new message
                boolean exist = false;
                InetAddress regAddress = RECEIVED_PACKET.getAddress(); // save address of client

                do {
                    SERVER_SOCKET.receive(RECEIVED_PACKET);
                } while (!RECEIVED_PACKET.getAddress().equals(regAddress)); // waiting to receive username

                String regUser = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength());

                for (SERVER_USER user : users)
                    if (regUser.equals(user.userName)) { // check all users for equal username
                        exist = true;

                        // send error to client
                        String error = "Error: client with this username is already exists";
                        SEND_BUFFER = error.getBytes(StandardCharsets.UTF_8);
                        SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                        SERVER_SOCKET.send(SEND_PACKET);

                        break;
                    }

                if (!exist) {
                    users.add(new SERVER_USER(regUser,null)); // adding new client to user list

                    // send registration success to client
                    String regMessage = "New user registered: " + regUser;
                    SEND_BUFFER = regMessage.getBytes(StandardCharsets.UTF_8);
                    SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                    SERVER_SOCKET.send(SEND_PACKET);

                    System.out.println(regMessage);
                }
            }


            /* Server received login command (/login) */

            else if (RECEIVED_DATA.equals(commands.LOG_USER)) {
                boolean exist = false;

                RECEIVE_BUFFER = new byte[256]; //clearing for a new message
                InetAddress loginAddress = RECEIVED_PACKET.getAddress();

                do {
                    SERVER_SOCKET.receive(RECEIVED_PACKET);
                } while (!RECEIVED_PACKET.getAddress().equals(loginAddress)); // waiting to receive username


                String loginUser = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength());

                for (SERVER_USER user : users) {
                    if (loginUser.equals(user.userName)) // check all users for equal username
                        exist = true;

                    if (user.isOnline) {
                        // send error to client: user is online
                        String error = "Error: client with this user is online";
                        SEND_BUFFER = error.getBytes(StandardCharsets.UTF_8);
                        SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                        SERVER_SOCKET.send(SEND_PACKET);
                        break;
                    }

                    if (exist) {
                        user.userAddress = RECEIVED_PACKET.getAddress();
                        user.isOnline = true;

                        // send login success to client
                        String loginMessage = "Login success: " + loginUser;
                        SEND_BUFFER = loginMessage.getBytes(StandardCharsets.UTF_8);
                        SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, user.userAddress, SERVER_PORT);
                        SERVER_SOCKET.send(SEND_PACKET);

                        System.out.println("User logged in: " + loginUser);
                        break;
                    }
                }

                if (!exist) {
                    // send error to client: user doesn't exists
                    String error = "Error: client with this username doesn't exists";
                    SEND_BUFFER = error.getBytes(StandardCharsets.UTF_8);
                    SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                    SERVER_SOCKET.send(SEND_PACKET);
                }
            }

            /* Server received user logout command (/logout) */

            else if (RECEIVED_DATA.equals(commands.LOGOUT_USER)) {
                for (SERVER_USER user : users) {
                    if (user.userAddress.equals(RECEIVED_PACKET.getAddress())) {
                        user.userAddress = null;
                        user.isOnline = false;

                        String logoutMessage = "Logout success: " + user.userName;
                        SEND_BUFFER = logoutMessage.getBytes(StandardCharsets.UTF_8);
                        SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                        SERVER_SOCKET.send(SEND_PACKET);
                        break;
                    }
                }
            }

            /* Server received end of work command (/end) */

            else if (RECEIVED_DATA.equals(commands.SERVER_END)) {
                String endingMessage = "Server has ended.";
                System.out.println(endingMessage);
                SEND_BUFFER = endingMessage.getBytes(StandardCharsets.UTF_8);

                // send to all users ending message
                for (SERVER_USER user : users) {
                    if (user.isOnline) {
                        SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, user.userAddress, SERVER_PORT);
                        SERVER_SOCKET.send(SEND_PACKET);
                    }
                }

                isRunning = false;
            }

            /* Server received user's message */

            else {
                String message = "not_initialized";

                for (SERVER_USER user : users) {
                    if (user.userAddress == RECEIVED_PACKET.getAddress() && user.isOnline) {
                        message = user.userName + ": " + RECEIVED_DATA;
                        SEND_BUFFER = message.getBytes(StandardCharsets.UTF_8);
                        break;
                    }
                }

                if (message.equals("not_initialized")) {
                    //send error to client
                    String error = "Error: please log in or register";
                    SEND_BUFFER = error.getBytes(StandardCharsets.UTF_8);
                    SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                    SERVER_SOCKET.send(SEND_PACKET);
                    continue;
                }

                System.out.println("Received from client: " + RECEIVED_DATA);

                // send message to all users except sender
                for (SERVER_USER user : users) {
                    if (user.userAddress != RECEIVED_PACKET.getAddress()) {
                        SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, user.userAddress, SERVER_PORT);
                        SERVER_SOCKET.send(SEND_PACKET);
                    }
                }
            }

            // clearing all buffers for receiving and sending new messages
            SEND_BUFFER = new byte[256];
            RECEIVE_BUFFER = new byte[256];
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
