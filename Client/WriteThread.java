package com.company;

import java.net.*;
import java.io.*;

/*
    Поток отвечает за отправление сообщений пользователя серверу
    Работает в бесконечном цикле. Чтобы закрыть - написать 'bye'
 */
public class WriteThread extends Thread{
    private PrintWriter writer;
    private Socket socket;
    private Client client;

    public WriteThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        }
        catch (IOException ex) {
            System.out.println("Output stream error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        Console console = System.console();

        String username = console.readLine("\nEnter your name: ");
        client.setUsername(username);
        writer.println(username);

        String text;

        do {
            text = console.readLine("[" + username + "]");
            writer.println(text);
        }
        while (!text.equals("bye"));

        try {
            socket.close();
        }
        catch (IOException ex) {
            System.out.println("Write to server error: " + ex.getMessage());
        }
    }
}
