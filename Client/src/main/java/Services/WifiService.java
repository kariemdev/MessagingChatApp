package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WifiService implements Runnable , Service {

    private Socket socket; // socket for the client
    private BufferedReader in; // read from server
    private PrintWriter out; // write to server

    private Scanner scanner ;

    public WifiService(Scanner sc ) {
        this.scanner = sc;


    } // This is the missing closing brace

    public void initConnection() {
        try {
            // creating socket
            socket = new Socket("localhost", 8082);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Wifi service available");
        } catch (IOException e) {
            System.out.println("Failed to connect to wifi server ");
        }
    }
    public void start() {
        System.out.println("Wifi service starting");
        initConnection();
    }
    public void run() {
        start();
        try {
            ServerListener serverListener = new ServerListener();
            Thread serverListenerThread = new Thread(serverListener);
            serverListenerThread.start();
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("!exit")) {
                    serverListenerThread.interrupt();
                    dc();
                    break;
                }
                out.println(input);
            }
        } catch (Exception e) {
            System.out.println("Error in receiving message from server");
        }
    }
    public void stop() {
        dc();
    }
    public void dc() {
        try {
            System.out.println("Disconnecting from wifi service ");
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("Error in closing wifi service");
        }
    }

    class ServerListener implements Runnable {
        // listen to server
        public void run() {
            String serverMessage;
            try {
                while (true) {
                    if (in == null) {
                        break;
                    }
                    serverMessage = in.readLine();
                    if (serverMessage != null) {
                        System.out.println("[Server] " + serverMessage);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in receiving message from server (Server went down)/client disconnected");
            }
        }

    }


}
