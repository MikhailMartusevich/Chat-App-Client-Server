package com.company;

import java.net.*;

public class Main {

    public static void main(String[] args) {

        final int port = 8888;  // Определение используемого порта

        try {
            // Создание сокета и определение IP адреса сервера
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
