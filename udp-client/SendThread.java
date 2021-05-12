package com.company;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


// Поток отвечает за отправку сообщений пользователя
public class SendThread extends Thread {

    // Конструктор получает клиентский сокет, номер порта и IP адрес сервера
    SendThread(DatagramSocket socket, int port, InetAddress ip) {
        this.socket = socket;
        this.port = port;
        this.ip = ip;
    }


    private InetAddress ip;
    private DatagramPacket packet;
    private DatagramSocket socket;
    private int port;
    private byte[] sendingDataBuffer = new byte[1024];
    String text = " ";


    // Тело потока
    public void run() {

        // Ввод имени клиента
        Scanner scanner = new Scanner(System.in);
        /*System.out.println("Представьтесь: ");  // По идее, эта строка идёт от сервера - нужен receive
        String username = scanner.nextLine();

        // Отправка имени серверу
        sendingDataBuffer = username.getBytes();
        packet = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, ip, port);

        try {
            socket.send(packet);
        }
        catch (IOException exp) {
            exp.printStackTrace();
        }*/

        // Формирование и отправка сообщений серверу
        while (!text.equals("Пока")) {

            System.out.println("Отправка сообщений началась");
            text = scanner.nextLine();
            sendingDataBuffer = text.getBytes();
            packet = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, ip, port);

            try {
                socket.send(packet);
                System.out.println("Отправлено");
            }
            catch (IOException exp) {
                exp.printStackTrace();
            }
        }

        System.out.println("Вы вышли из чата");
        socket.close();
    }
}
