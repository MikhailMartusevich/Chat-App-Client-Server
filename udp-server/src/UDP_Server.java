import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class UDP_Server {

    public UDP_Server() throws IOException {
        SERVER_SOCKET = new DatagramSocket(SERVER_PORT);
    }

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

        public SERVER_USER(String _name, InetAddress _inetaddr, boolean _isonline) {
            userName = _name;
            userAddress = _inetaddr;
            isOnline = _isonline;
        }
    }

    public final static int          SERVER_PORT = 8888;         // ServerSocket port
    private static DatagramSocket    SERVER_SOCKET;              // UDP socket
    private boolean                  isRunning;                  // server status variable
    private boolean                  loggedUser;                 // client status

    private byte[]  RECEIVE_BUFFER  = new byte[256];             // receiving messages buffer
    private byte[]  SEND_BUFFER     = new byte[256];             // sending messages buffer

    DatagramPacket  RECEIVED_PACKET;
    DatagramPacket  SEND_PACKET;

    String          RECEIVED_DATA;

    private final SERVER_COMMANDS commands = new SERVER_COMMANDS();
    private ArrayList<SERVER_USER> users = new ArrayList<>();

    private void register_user() throws IOException {
        RECEIVE_BUFFER = new byte[256]; //clearing for a new message

        String[] command = RECEIVED_DATA.split(" ", 2);
        boolean exist = false;

        if (command.length != 2)
            return;

        String regUser = command[1];

        for (SERVER_USER user : users)
            if (regUser.equals(user.userName)) { // check all users for equal username
                exist = true;

                // send error to client
                String error = "Error: client with this username is already exists";
                SEND_BUFFER = error.getBytes();
                SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                SERVER_SOCKET.send(SEND_PACKET);
                break;
            }
        if (!exist) {
            users.add(new SERVER_USER(regUser, null, false)); // adding new client to user list

            // send registration success to client
            String regMessage = "New user registered: " + regUser;
            SEND_BUFFER = regMessage.getBytes();
            SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
            SERVER_SOCKET.send(SEND_PACKET);
            System.out.println(regMessage);
        }
    }
    private void login_user() throws IOException {
        RECEIVE_BUFFER = new byte[256]; //clearing for a new message
        boolean exist = false;
        String[] command = RECEIVED_DATA.split(" ",2);

        if (command.length != 2)
            return;

        String loginUser = command[1];

        for (SERVER_USER user : users) {
            if (loginUser.equals(user.userName)) { // check all users for equal username
                exist = true;

                if (user.isOnline) {
                    // send error to client: user is online
                    String error = "Error: client with this username is online";
                    SEND_BUFFER = error.getBytes();
                    SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                    SERVER_SOCKET.send(SEND_PACKET);
                    break;
                }
            }
            if (exist) {
                user.userAddress = RECEIVED_PACKET.getAddress();
                user.isOnline = true;

                // send login success to client
                String loginMessage = "Login success: " + loginUser;
                SEND_BUFFER = loginMessage.getBytes();
                SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, user.userAddress, SERVER_PORT);
                SERVER_SOCKET.send(SEND_PACKET);

                System.out.println("User logged in: " + loginUser);
                break;
            }
        }
        if (!exist) {
            // send error to client: user doesn't exists
            String error = "Error: client with this username doesn't exists";
            SEND_BUFFER = error.getBytes();
            SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
            SERVER_SOCKET.send(SEND_PACKET);
        }

    }
    private void logout_user() throws IOException {
        for (SERVER_USER user : users) {
            if (user.userAddress != null &&
                    user.userAddress.equals(RECEIVED_PACKET.getAddress())) {
                user.userAddress = null;
                user.isOnline = false;
                String logoutMessage = "Logout success: " + user.userName;

                SEND_BUFFER = logoutMessage.getBytes();
                SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
                SERVER_SOCKET.send(SEND_PACKET);

                System.out.println("User logged out: " + user.userName);
                break;
            }
        }

    }
    private void server_end() throws IOException {
        String endingMessage = "Server has ended.";
        System.out.println(endingMessage);
        SEND_BUFFER = endingMessage.getBytes();

        // send to all users ending message
        for (SERVER_USER user : users) {
            if (user.isOnline) {
                SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, user.userAddress, SERVER_PORT);
                SERVER_SOCKET.send(SEND_PACKET);
            }
        }
        isRunning = false;
    }
    private void send_message() throws IOException {
        String message = "not_initialized";

        for (SERVER_USER user : users) {
            if (user.userAddress != null &&
                    user.userAddress.equals(RECEIVED_PACKET.getAddress()) && user.isOnline) {
                message = user.userName + ": " + RECEIVED_DATA;
                SEND_BUFFER = message.getBytes();
                break;
            }
        }

        if (message.equals("not_initialized")) {
            System.out.println("Message from unknown user: " + RECEIVED_PACKET.getAddress());

            //send error to client
            String error = "Error: please log in or register";
            SEND_BUFFER = error.getBytes();
            SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, RECEIVED_PACKET.getAddress(), SERVER_PORT);
            SERVER_SOCKET.send(SEND_PACKET);
            return;
        }

        // send message to all users except sender
        for (SERVER_USER user : users) {
            if (user.userAddress != null &&
                    !user.userAddress.equals(RECEIVED_PACKET.getAddress()) && user.isOnline) {
                SEND_PACKET = new DatagramPacket(SEND_BUFFER, SEND_BUFFER.length, user.userAddress, SERVER_PORT);
                SERVER_SOCKET.send(SEND_PACKET);
            }
        }
    }
    private void receive_message() throws IOException {
        loggedUser = false;

        RECEIVED_PACKET = new DatagramPacket(RECEIVE_BUFFER, RECEIVE_BUFFER.length); // UDP receiving packet

        SERVER_SOCKET.receive(RECEIVED_PACKET); // receive data from client

        RECEIVED_DATA = new String(RECEIVED_PACKET.getData(), 0, RECEIVED_PACKET.getLength()); // convert received data to string

        System.out.println(RECEIVED_PACKET.getAddress() + ": " + RECEIVED_DATA);

        for (SERVER_USER user : users) // check IP address for logged user
            if (user.userAddress != null &&
                    user.userAddress.equals(RECEIVED_PACKET.getAddress())) {
                loggedUser = true;
                break;
            }
    }

    public void Run() throws IOException {
        isRunning = true;
        System.out.println("Server started");
        System.out.println("Waiting for a client message... ");

            while (isRunning) {
                // clearing all buffers for receiving and sending new messages
                SEND_BUFFER = new byte[256];
                RECEIVE_BUFFER = new byte[256];

                receive_message();

                /* Server received registration command (/reg)*/
                if (RECEIVED_DATA.startsWith(commands.REG_USER) && !loggedUser)
                    register_user();


                /* Server received login command (/login) */
                else if (RECEIVED_DATA.startsWith(commands.LOG_USER) && !loggedUser)
                    login_user();


                /* Server received user logout command (/logout) */
                else if (RECEIVED_DATA.equals(commands.LOGOUT_USER) && loggedUser)
                    logout_user();


                /* Server received end of work command (/end) */
                else if (RECEIVED_DATA.equals(commands.SERVER_END))
                    server_end();


                /* Server received user's message */
                else
                    send_message();

            }

        SERVER_SOCKET.close(); // close socket after failing the condition
    }

    public static void main(String[] args) {
        try {
            UDP_Server server = new UDP_Server();
            server.Run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
