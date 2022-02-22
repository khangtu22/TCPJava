package frontend.client.controllers;

import backend.model.User;
import frontend.DomainConstraint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author khang.tran
 * @overview represents the data manager class responsible for managing the student objects.
 * @attributes students    ArrayList
 * gui         JFrame
 * @abstract_properties <pre>
 *  optional(students) = false /\
 *  optional(gui) = false
 *  </pre>
 */
public class ClientManager extends WindowAdapter implements ActionListener {

    private static final long serialVersionUID = -6500665823330706018L;

    private final Socket clientSocket;
    private final String STORAGE_FILE = "//Users//khangtu//IdeaProjects//CourseMan//src//courseman2//courseman2.controllers//module.dat";
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private ArrayList<User> students;

    // view elements
    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JFrame gui;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JFrame frame;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JPanel north;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JPanel south;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JPanel centre;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JLabel title;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JButton ok;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JButton cancel;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JLabel lbl1, lbl2, lbl3, lbl4;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private JTextField tf1, tf2, tf3, tf4;

    @DomainConstraint(type = DomainConstraint.Type.Object, optional = false)
    private ObjectOutputStream os;

    /**
     * @effects initialise <tt>this</tt> with an empty set of students <br>
     * {@link #createGUI()}: create <tt>gui</tt>
     */
    public ClientManager(Socket clientSocket) {
        this.clientSocket = clientSocket;
        students = new ArrayList<>();
//        os = new ObjectOutputStream(socket.getOutputStream());
        createGUI();
    }

    /**
     * @modifies this.gui
     * @effects create <tt>gui</tt> as required (mockup image) <br>
     * The action listener of the buttons is <tt>this</tt>.
     */
    public void createGUI() {
        // Main GUI
        gui = new JFrame("Login Form");
        gui.setPreferredSize(new Dimension(550, 250));

        // North panel
        north = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gui.add(north, BorderLayout.NORTH);
        JPanel centrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centre = new JPanel(new GridLayout(4, 2));
        centrePanel.add(centre);
        gui.add(centrePanel, BorderLayout.CENTER);

        // South panel
        south = new JPanel();
        gui.add(south, BorderLayout.SOUTH);
        title = new JLabel("Enter details:");
        north.add(title);

        // the buttons
        ok = new JButton("LOGIN");
        ok.addActionListener(this);

        cancel = new JButton("CANCEL");
        cancel.addActionListener(this);

        south.add(ok);
        south.add(cancel);

        // LABELS STUDENT TRAIT
        lbl1 = new JLabel("Username:(*)");
        lbl2 = new JLabel("Password:(*) ");

        // the text fields
        tf1 = new JTextField();
        tf2 = new JTextField(20);

        // bind labels to text fields
        lbl1.setLabelFor(tf1);
        lbl2.setLabelFor(tf2);

        centre.add(lbl1);
        centre.add(tf1);
        centre.add(lbl2);
        centre.add(tf2);

        // set up window
        gui.pack();
        gui.setLocationRelativeTo(null);
    }

    /**
     * @requires <tt>gui != null</tt>
     * @effects show <tt>gui</tt>
     */
    public void display() {
        gui.setVisible(true);
    }

    /**
     * @effects handles user actions on the buttons
     *
     * <pre>
     *          if button is OK
     *            {@link #createUser()}: create a new student from form data
     *          else if button is Cancel
     *            {@link #clearInput()}: reset form fields
     *          </pre>
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "LOGIN" -> {
                try {
                    createUser();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            case "CLEAR" -> clearInput();
//            case "EXIT" -> shutDown();
        }
    }


    /**
     * @effects <pre>
     *  create a new student from the data in the data panel of gui and add it to <tt>this.students</tt>
     *
     *  if not success (exception is thrown)
     *      display the exception message on the GUI dialog
     *          </pre>
     */
    private void createUser() throws IOException {
        try (OutputStream outputStream = clientSocket.getOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(outputStream);
             InputStream inputStream = clientSocket.getInputStream();
             ObjectInputStream ois = new ObjectInputStream(inputStream);) {

            String username = tf1.getText();
            String password = tf2.getText();
            System.out.println("User try to login: " + username);
            if (username.equals("") || password.equals("")) {
                JOptionPane.showMessageDialog(frame, "Login fail!!");
            } else {
                User user = new User(username, password);
                oos.writeUTF("login");
                oos.flush();

                oos.writeObject(user);
                oos.flush();

                String loginMessage = ois.readUTF();
                System.out.println("Is login success: " + loginMessage );
                if(loginMessage.equals("success")){
                    JOptionPane.showMessageDialog(frame, "Login successful: " + user.getUsername());
                } else {
                    JOptionPane.showMessageDialog(frame, "Login fail!!");
                }

            }
        }

    }

    /**
     * @requires this.tfName != null /\ this.tfDob != null /\ this.tfAddress != null
     * /\ this.tfEmail != null
     */
    private void clearInput() {
        Component[] comps = centre.getComponents();
        Component comp;
        JTextField tf;

        for (Component component : comps) {
            comp = component;
            if (comp instanceof JTextField) {
                // found a text field
                tf = (JTextField) comp;

                // only clear the enabled text fields
                if (tf.isEnabled())
                    tf.setText("");
            } else if (comp instanceof JPanel) {
                clearInput();
            }
        }
    }

    /**
     * @requires <tt>gui != null</tt>
     * @effects hide <tt>gui</tt>
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (!gui.isVisible()) {
            gui.setVisible(false);
            System.out.println("StudentManager: closed!");
        }
    }

    /**
     * @requires <tt>students != null</tt>
     * @modifies this
     * @effects load into <tt>students</tt> the data students in the storage file
     * <tt>students.dat</tt> that was saved before.
     *
     * <pre>
     *          if succeeds
     *            display a console message
     *          else
     *            display a console error message
     *          </pre>
     */
    public void startUp() {
        if (students != null) {
            try (FileInputStream fis = new FileInputStream("students.dat");
                 ObjectInputStream ois = new ObjectInputStream(fis);) {
                try {
                    while (true) {
                        User s = (User) ois.readObject();
                        students.add(s);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Students loaded " + students.size() + " objects");
                    System.out.println("Reading students finished!");
                }
            } catch (IOException e) {
                System.out.println("Reading Students finished! File is empty!!!");
            }
        }
    }

    /**
     * @requires <tt>gui != null</tt>
     * @modifies this
     * @effects <pre>
     *          dispose <tt>gui</tt> and clear <tt>students</tt>
     *          store <code>this.students</code> to file and shutdown the application
     *          </pre>
     */
    public void shutDown() {
        if (this.gui != null) {
            this.gui.dispose();
            try (FileOutputStream fos = new FileOutputStream("students.dat");
                 ObjectOutputStream oos = new ObjectOutputStream(fos);) {
                students.stream().forEach(s -> {
                    try {
                        oos.writeObject(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                System.out.println("StudentManager.shutDown ...stored " + students.size() + " objects");
                students.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
