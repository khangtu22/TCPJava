//package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ServerThread implements Runnable {

    private static final ArrayList<ServerThread> instances = new ArrayList<>();
    private final Socket clientSocket;

    public ServerThread(Socket socket) {
        clientSocket = socket;
        addInstance();
    }

    private static synchronized void dispatch(String message) {
        for (ServerThread serverThread : instances) {
            serverThread.dispatchMessage(message);
        }
    }

    private static synchronized String handleEcho(String echoString) {
        echoString = echoString.replace("echo \"", "");
        return removeLastChar(echoString);
    }

    private static synchronized String handleStandardize(String echoString) {
        echoString = echoString.replace("standardize \"", "");
        echoString = removeLastChar(echoString);
        echoString = echoString.trim().replaceAll(" +", " ");
        echoString = echoString.toLowerCase();
        echoString = echoString.substring(0, 1).toUpperCase() + echoString.substring(1);
        return echoString;
    }

    private static synchronized String removeLastChar(String s) {
        return s.substring(0, s.length() - 1);
    }

    private synchronized void addInstance() {
        instances.add(this);
    }

    private synchronized void removeInstance() {
        instances.remove(this);
    }

    private synchronized void dispatchMessage(String echoString) {
        String patternEcho = "echo \".*\"";
        String patternStandardize = "standardize \".*\"";
        String serverNotation = "SERVER";
        try {
            OutputStream out = this.clientSocket.getOutputStream();
            PrintWriter pw = new PrintWriter(out, true);

            if (echoString != null) {
                boolean matchEcho = Pattern.matches(patternEcho, echoString);
                boolean matchStandardize = Pattern.matches(patternStandardize, echoString);
                if (matchEcho) {
                    pw.println(serverNotation + ">>> " + handleEcho(echoString));
                } else if (matchStandardize) {
                    pw.println(serverNotation + ">>> " + handleStandardize(echoString));
                } else {
                    pw.println("SERVER>>> Command not found!");
                }
            }
        } catch (IOException e) {
            System.out.println("dispatchMessage caught exception :(");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        InputStream inputStream;
        OutputStream outputStream;
        PrintWriter pw;
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
            pw = new PrintWriter(outputStream, true);
            Scanner in = new Scanner(inputStream);
            String clientName = in.nextLine();
            pw.println("SERVER>>> Hello " + clientName);
            String inMessage;

            while (true) {
                inMessage = in.nextLine();

                if (inMessage.equals("exit")) {
                    pw.println("\nGood bye");
                    removeInstance();
                    System.out.println("Client disconnected!");
                }

                System.out.printf("Received: '%s'.\n", inMessage);
                dispatch(inMessage);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
