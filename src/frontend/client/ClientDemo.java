package frontend.client;

import backend.model.Message;
import frontend.DomainConstraint;
import frontend.client.controllers.ClientManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

/**
 * @author khang.tran
 * @overview Represents the main class of the CourseMan program.
 * @attributes sman StudentManager
 * mman ModuleManager
 * gui JFrame
 * @abstract_properties optional(sman) = false /\
 * optional(mman) = false /\
 * optional(gui) = false
 */
public class ClientDemo extends WindowAdapter implements ActionListener {

    private final String chatServer;
    //    private final ObjectOutputStream os;
    private Socket client;
    private JTextArea displayArea;
    private JTextField enterField;
    private JLabel enterFieldLabel;
    private ObjectOutputStream os;
    private ObjectInputStream oi;
    private String message = "";

    @DomainConstraint(type = DomainConstraint.Type.String, optional = false)
    private final ClientManager cman;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JFrame gui;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenuBar menuBar;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenu jm1;
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenu jm2;
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenu jm3;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenuItem jmi1;
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenuItem jmi2;
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenuItem jmi3;
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JMenuItem jmi4;

    /**
     * @effects initialise <tt>sman, mman</tt> <br>
     * {@link #createGUI()}: create <tt>gui</tt>
     */
    public ClientDemo() throws IOException {
        chatServer = "localhost";
        createGUI();
        connectToServer();
        cman = new ClientManager(client);
    }

    /**
     * The run method
     *
     * @effects create an instance of <tt>CourseManDemo</tt> {@link #startUp()}:
     * start up the <tt>CourseManDemo</tt> instance {@link #display()}:
     * display the main gui of <tt>CourseManDemo</tt> instance
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ClientDemo app = new ClientDemo();
        app.startUp();
        app.display();
    }

    /**
     * @modifies this.gui
     * @effects create <tt>gui</tt> that has a menu bar with:
     * <ol>
     * <li>File menu has one item: Exit
     * <li>Student menu has one item: New Student (to create a new student)
     * <li>Module menu has one item: New Module (to create a new module)
     * </ol>
     * The action listener of the menu items is <tt>this</tt>.
     */
    public void createGUI() {
        gui = new JFrame();
        gui.setPreferredSize(new Dimension(400, 300));
        //menu bar
        menuBar = new JMenuBar();

        jm1 = new JMenu("File");
        jm2 = new JMenu("Student");
        jm3 = new JMenu("Module");

        jmi1 = new JMenuItem("Exit");
        jmi2 = new JMenuItem("New Student");
        jmi3 = new JMenuItem("New Module");
        jmi4 = new JMenuItem("Login");

        jmi1.addActionListener(this);
        jmi2.addActionListener(this);
        jmi3.addActionListener(this);
        jmi4.addActionListener(this);

        jm1.add(jmi1);
        jm2.add(jmi2);
        jm3.add(jmi3);
        jm1.add(jmi4);

        menuBar.add(jm1);
        menuBar.add(jm2);
        menuBar.add(jm3);

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

        panel.add(enterFieldLabel, BorderLayout.WEST);
        panel.add(enterField, BorderLayout.CENTER);

        displayArea = new JTextArea();

        gui.setJMenuBar(menuBar);
        gui.add(new JScrollPane(displayArea), BorderLayout.CENTER);
        gui.add(panel, BorderLayout.NORTH);
        gui.pack();
    }

    /**
     * @effects show <tt>gui</tt>
     */
    public void display() {
        gui.setVisible(true);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            shutDown();
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    // connect to server
    private void connectToServer() {
        displayMessage("Trying to connect...\n");
        try {
            client = new Socket(InetAddress.getByName(chatServer), 9091);
            displayMessage("Connected to: " + client.getInetAddress().getHostName());
        } catch (IOException e) {
            displayMessage("Connection refused. Please check server connection!");
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

    public void runClient() {
        try {
            connectToServer();
            while (!Objects.equals(message, "exit")) {
                getStreams();
                processConnection();
            }
        } catch (EOFException eofException) {
            displayMessage("\nClient terminated connection");
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // process connection with server
    private void processConnection() throws IOException, ClassNotFoundException {
        setTextFieldEditable(true);
        Message messageIn = (Message) oi.readObject();
        message = messageIn.getText();
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
            oi.close();
            os.close();
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
                os.writeObject(new Message(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (message.equals("exit")) {
                Message goodbyeMessage = (Message) oi.readObject();
                displayMessage(goodbyeMessage.getText());
            } else {
                displayMessage("\nCLIENT>>> " + message);
            }
        } catch (IOException e) {
            displayMessage("Error IO!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    // send and receive data
    private void getStreams() throws IOException, ClassNotFoundException {
        os = new ObjectOutputStream(client.getOutputStream());
        oi = new ObjectInputStream(client.getInputStream());
        processConnection();
    }

    /**
     * @effects handles user actions on the menu items
     *
     * <pre>
     *          if menu item is Student/New Student
     *            .display()}
     *          else if menu item is Module/New Module
     *            .display()
     *          else if menu item is File/Exit
     *            {@link #shutDown()}
     *          </pre>
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "Exit":
                try {
                    shutDown();
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
                break;
            case "Login":
                cman.display();
                break;
            case "New Student":
//                sman.display();
                break;
            case "New Module":
//                mman.display();
                break;
        }
    }

    /**
     * @effects start up <tt>sman, mman</tt>
     */
    public void startUp() throws IOException, ClassNotFoundException {
        System.out.println("Starting up...");
//        sman.startUp();
//        mman.startUp();
    }

    /**
     * @effects shut down <tt>sman, mman</tt> <br>
     * dispose <tt>gui</tt> and end the program.
     */
    public void shutDown() throws IOException, ClassNotFoundException {
        System.out.println("Shutting down...");
        gui.dispose();
//        sman.shutDown();
//        mman.shutDown();
    }
}
