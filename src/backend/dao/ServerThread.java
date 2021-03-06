package backend.dao;

import backend.model.Message;
import backend.model.User;
import connect.DBConnection;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ServerThread implements Runnable {

    public static final String USER_CREATE = "INSERT INTO user (name, password) VALUES (?, ?);";
    private static final ArrayList<ServerThread> instances = new ArrayList<>();
    private static final String USER_LOGIN = "SELECT * FROM user WHERE name = ? and password = ?";
    private static final String GET_USER_BY_USERNAME = "SELECT * FROM user WHERE name = ?";
    private final Socket clientSocket;

    public ServerThread(Socket socket) {
        clientSocket = socket;
        addInstance();
    }

    private static synchronized void dispatch(Message message, ObjectOutputStream oos) {
        for (ServerThread serverThread : instances) {
            serverThread.dispatchMessage(message, oos);
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

    private synchronized void dispatchMessage(Message message, ObjectOutputStream oos) {
        String echoString = message.getMessage();
        String patternEcho = "echo \".*\"";
        String patternStandardize = "standardize \".*\"";
        String serverNotation = "SERVER";
        try {
//            OutputStream out = this.clientSocket.getOutputStream();
//            ObjectOutputStream oss = new ObjectOutputStream(out);

            if (echoString != null) {
                boolean matchEcho = Pattern.matches(patternEcho, echoString);
                boolean matchStandardize = Pattern.matches(patternStandardize, echoString);
                if (matchEcho) {
                    String tempEcho = handleEcho(echoString);
                    Message messageEcho = new Message(tempEcho);
                    oos.writeObject(messageEcho);
                    oos.flush();
                } else if (matchStandardize) {
                    String tempEcho = handleStandardize(echoString);
                    Message messageEcho = new Message(tempEcho);
                    oos.writeObject(messageEcho);
                    oos.flush();
                } else {
                    Message messageEcho = new Message("Command not found!");
                    oos.writeObject(messageEcho);
                    oos.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("dispatchMessage caught exception :(");
            e.printStackTrace();
        }
    }

    private synchronized User checkLogin(User user) throws SQLException {
        try (DBConnection dbHelper = DBConnection.getDBHelper();
             Connection connection = dbHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(USER_LOGIN)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            ResultSet result = statement.executeQuery();
            User existedUser = null;
            if (result.next()) {
                existedUser = new User();
                existedUser.setUsername(result.getString("name"));
            }
            return existedUser;
        }
    }


    private synchronized User isUserExisted(User user) throws SQLException {
        User existedUser = null;
        try (DBConnection dbHelper = DBConnection.getDBHelper();
             Connection connection = dbHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_USER_BY_USERNAME)) {
            statement.setString(1, user.getUsername());
            ResultSet results = statement.executeQuery();
            if (results.next()){
                existedUser = new User();
                existedUser.setUsername(results.getString("name"));
            }
            return existedUser;
        }
    }


    private synchronized Boolean createUser(User user) throws SQLException {
        boolean rowUpdated = false;
        try (DBConnection dbHelper = DBConnection.getDBHelper();
             Connection connection = dbHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(USER_CREATE)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            rowUpdated = statement.executeUpdate() > 0;

            return rowUpdated;
        }
    }

    @Override
    public void run() {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            ObjectOutputStream oss = new ObjectOutputStream(outputStream);

            Message message;
            while (true) {
                String action = ois.readUTF();
                System.out.println("Action: " + action);
                switch (action) {
                    case "Login" -> {
                        User user = (User) ois.readObject();
                        System.out.println("Inside login: " + user.toString());
                        User existedUser = checkLogin(user);
                        if (existedUser != null) {
                            System.out.println("Login successful!");
                            oss.writeUTF("Login succeed");
                            oss.flush();
                        } else {
                            System.out.println("Fail to login!");
                            oss.writeUTF("Wrong username or password");
                            oss.flush();
                        }
                    }

                    case "Register" -> {
                        User registerUser = (User) ois.readObject();
                        User isUserExisted = isUserExisted(registerUser);
                        if (isUserExisted == null) {
                            boolean isCreated = createUser(registerUser);
                            if (isCreated) {
                                System.out.println("Create user successful!");
                                oss.writeUTF("success");
                            } else {
                                System.out.println("Fail to create user!");
                                oss.writeUTF("fail");
                            }
                        } else {
                            oss.writeUTF("Username already existed");
                        }
                        oss.flush();
                    }
                    case "Message" -> {
                        message = (Message) ois.readObject();

                        if (message.getMessage().equalsIgnoreCase("logout")) {
                            oss.writeObject(message);
                            removeInstance();
                            oss.flush();
                            System.out.println("frontend.Client disconnected!");
                        }

                        System.out.printf("Received: '%s'.\n", message);
                        dispatch(message, oss);
                    }
                }
                /*ois.close();
                clientSocket.close();
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " disconnect from server...");*/
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }
}
