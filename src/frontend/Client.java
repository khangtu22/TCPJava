package frontend;//package com.company;

import backend.model.User;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9091)) {
            System.out.print("Enter clientName: ");
            BufferedReader echoes = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter stringToEcho = new PrintWriter(socket.getOutputStream(), true);


            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            User user = new User("khang", "123456");
            // send action message
            os.writeUTF("login");
            os.flush();

            os.writeObject(user);
            os.flush();
            Scanner scanner = new Scanner(System.in);

            String echoString;
            String response;

            do {
                echoString = scanner.nextLine();

                stringToEcho.println(echoString);
                if(!echoString.equals("exit")) {
                    response = echoes.readLine();
                    System.out.println(response);
                }
            } while(!echoString.equals("exit"));


        } catch (IOException e) {
            System.out.println("frontend.Client Error: " + e.getMessage());
        }
    }
}