package com.company;

import java.net.*;
import java.io.*;

public class Client {
    private String hostname;    // имя хоста
    private String username;    // имя пользователя
    private int port;           // используемый порт

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Successful connection");

            //TODO: потоки ввода / вывода
            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();
        }
        catch (UnknownHostException ex) {
            System.out.println("Server now found: " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }

    void setUsername(String username) { this.username = username; }
    String getUsername() { return this.username; }
}
