package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


// Поток отвечает за принятие сообщений от сервера
public class ReceiveThread extends Thread {

    // Конструктор получает клиентский сокет, номер порта и IP адрес сервера
    ReceiveThread(DatagramSocket socket) {
        this.socket = socket;
    }


    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte[] receivingDataBuffer = new byte[1024];
    private String text;


    // Тело потока
    public void run() {

        while(true) {

            // Принятие сообщений от сервера
            packet = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
            try {
                socket.receive(packet);
            }
            catch(IOException exp) {
                exp.printStackTrace();
            }

            // Вывод сообщения на экран
            text = new String(packet.getData());
            System.out.println(text);
        }
    }
}
