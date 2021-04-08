package com.company;

import java.net.*;     // Сетевая библиотека
import java.io.*;      // Библиотека ввода/вывода

/*
    Поток отвечает за принятие сообщений от сервера.
    Работает в бесконечном цикле до закрытия клиента.
 */
public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private Client client;

    public ReadThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        }
        catch (IOException ex) {
            System.out.println("Input stream error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                System.out.println("\n" + response);

                if (client.getUsername() != null) {
                    System.out.println("[" + client.getUsername() + "]");
                }
            }
            catch (IOException ex) {
                System.out.println("Read from server error" + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}
