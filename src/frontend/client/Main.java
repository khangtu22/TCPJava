package frontend.client;

public class Main {

    public static void main(String[] args) {
        TCPClientGUI tcpClientGUI = new TCPClientGUI("localhost");
        tcpClientGUI.runClient();
    }
}
