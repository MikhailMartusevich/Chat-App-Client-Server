package com.company;

public class Main {

    public static void main(String[] args) {

        String hostname = "127.0.0.1";
        int port = Integer.parseInt("8989");

        Client client = new Client(hostname, port);
        client.execute();
    }
}
