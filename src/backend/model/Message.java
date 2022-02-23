package backend.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Scanner;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = -6500665823330706018L;
    private String message;

    public Message() {
    }

    public Message(String text) {
        this.message = text;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void inputMessage() {
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter Message");
        this.message = myObj.nextLine();  // Read massage input
    }
}
