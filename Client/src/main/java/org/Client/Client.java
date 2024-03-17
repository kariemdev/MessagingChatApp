package org.Client;

import java.util.Scanner;

import Services.*;


public class Client  {

    private Scanner sc = new Scanner(System.in);
    private Service currentService; // current service running

    public void  parseCommand(String command) {
        interruptCurrentService();
        switch (command) {
            case "!wifi":
                interruptCurrentService();
                WifiService wifiClient = new WifiService(sc);
                currentService = wifiClient;
                currentService.run();
                break;
            case "!emailSupport":
                interruptCurrentService();
                System.out.println("Set the title of the email");
                String title = sc.nextLine();
                System.out.println("Set the message of the email");
                String message = sc.nextLine();
                MailService emailClient = new MailService("user", title, message);
                Thread emailClientThread = new Thread(emailClient);
                emailClientThread.start();
                System.out.println("Email sent");
                break;
            case "!exit":
                System.out.println("Exiting");
                break;
            default:
                System.out.println("Invalid service , do !wifi or !emailSupport");
                break;
        }
    }
    public void interruptCurrentService() {
        if (currentService != null) {
            currentService.dc();
            currentService.stop();

        }

    }



    public static void main(String[] args) {
        Client client = new Client();
        String command = "";
        System.out.println("Enter a command");
        while (!command.equals("!exit")) {

            if (client.sc.hasNextLine()) {
                command = client.sc.nextLine();
                client.parseCommand(command);
            }
        }

    }

}
