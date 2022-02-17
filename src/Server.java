//package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private int port;

    public Server() {
    }

    public Server(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            init(port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void init(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void loop() throws IOException {
        Socket client;
        while (true) {
            client = serverSocket.accept();
            System.out.println("Got connection from: " + client.getInetAddress());

            ServerThread serverThread = new ServerThread(client);
            Thread thread = new Thread(serverThread);
            thread.start();
        }
    }

    public void run() {
        try {
            init(this.port);
            loop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
