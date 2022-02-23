package frontend.client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public class TCPClientGUI extends JFrame {
    private final JTextField enterField;
    private final JLabel enterFieldLabel;
    private final JTextArea displayArea;
    private final String chatServer;
    private final JLabel name;
    private final JLabel password;
    private final JTextField tname;
    private final JTextField tpassword;
    private PrintWriter output;
    private BufferedReader input;
    private String message = "";
    private Socket client;
    private Container c;
    private JButton sub;
    private String userName = "CLIENT";

    public TCPClientGUI(String host) {
        super("TCP Client");
//        client = clientSocket;

        chatServer = host;

        enterFieldLabel = new JLabel("Enter command: ");
        enterFieldLabel.setFont(new Font("Arial", Font.BOLD, 18));
        enterField = new JTextField();
        enterField.setEditable(true);
        enterField.setFont(new Font("Arial", Font.BOLD, 14));

        enterField.addActionListener(event -> {
            try {
                sendData(event.getActionCommand());
            } catch (IOException e) {
                e.printStackTrace();
            }
            enterField.setText("");
        });

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new BorderLayout());

        panel.add(enterFieldLabel, BorderLayout.WEST);
        panel.add(enterField, BorderLayout.CENTER);
        add(panel, BorderLayout.NORTH);

        name = new JLabel("Enter your name: ");
        name.setFont(new Font("Arial", Font.PLAIN, 20));
        name.setSize(100, 20);
        name.setLocation(100, 100);

        password = new JLabel("Enter your password: ");
        password.setFont(new Font("Arial", Font.PLAIN, 20));
        password.setSize(100, 20);
        password.setLocation(100, 100);

        tname = new JTextField();
        tname.setFont(new Font("Arial", Font.PLAIN, 15));
        tname.setSize(190, 20);
        tname.setLocation(200, 100);

        tpassword = new JTextField();
        tpassword.setFont(new Font("Arial", Font.PLAIN, 15));
        tpassword.setSize(190, 20);
        tpassword.setLocation(200, 100);

        panel.setVisible(false);

        tname.addActionListener(event -> {
            try {
                name.setVisible(false);
                tname.setVisible(false);
                panel.setVisible(true);
                panel2.setVisible(false);
                sendUsernameData(event.getActionCommand());

            } catch (IOException e) {
                e.printStackTrace();
            }
            enterField.setText("");
        });

        panel2.add(name, BorderLayout.WEST);
        panel2.add(tname, BorderLayout.CENTER);

        add(panel2, BorderLayout.SOUTH);

        displayArea = new JTextArea();
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setSize(500, 400);
        setLocationRelativeTo(null);
        setVisible(true);
        runClient();
    }

    // connect to server and process messages
    public void runClient() {
        try {
            connectToServer();
            while(!Objects.equals(message, "exit")) {
                getStreams();
                processConnection();
            }
        } catch (EOFException eofException) {
            displayMessage("\nClient terminated connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // connect to server
    private void connectToServer() {
        displayMessage("Trying to connect...\n");
        try {
            client = new Socket(InetAddress.getByName(chatServer), 9091);
        } catch (IOException e) {
            displayMessage("Connection refused. Please check server connection!");
        }
        displayMessage("Connected to: " + client.getInetAddress().getHostName());
    }

    // send and receive data
    private void getStreams() throws IOException {
        output = new PrintWriter(client.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        processConnection();
    }

    // process connection with server
    private void processConnection() throws IOException {
        setTextFieldEditable(true);
        message = input.readLine();
        if (message != null) {
            if (!message.equals("exit")) {
                displayMessage("\n" + message);
            } else {
                closeConnection();
            }
        }
    }

    // close streams and socket
    private void closeConnection() {
        setTextFieldEditable(false);
        try {
            output.close();
            input.close();
            client.close();
            displayMessage("Closed connection.");
        } catch (IOException | NullPointerException ioException) {
            System.out.println("Error close!");
        }
    }

    // send message to server
    private void sendData(String message) throws IOException {
        String response;
        try {
            synchronized (this) {
                output.println(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (message.equals("exit")) {
                response = input.readLine();
                displayMessage(response);
            } else {
                displayMessage("\n" + userName + ">>> " + message);
            }
        } catch (IOException e) {
            displayMessage("Error IO!");
        }

    }

    private void sendUsernameData(String message) throws IOException {
        String response;
        output.println("clientName: " + message);
        this.userName = message;
        try {
            if (message.equals("exit")) {
                response = input.readLine();
                displayMessage(response);
            }
        } catch (IOException e) {
            displayMessage("Error IO!");
        }
    }

    // manipulates displayArea in the event-dispatch thread
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                displayArea.append(messageToDisplay);
            }
        });
    }

    // manipulates enterField in the event-dispatch thread
    private void setTextFieldEditable(final boolean editable) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                enterField.setEditable(editable);
            }
        });
    }
}