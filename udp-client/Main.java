package com.company;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        final int port = 8080;
        String text = " ";
        Scanner scanner = new Scanner(System.in);
        byte[] sendingDataBuffer = new byte[1024];
        DatagramPacket packet;

        try {
            DatagramSocket socket = new DatagramSocket(port);
            InetAddress ip = InetAddress.getLocalHost();

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
