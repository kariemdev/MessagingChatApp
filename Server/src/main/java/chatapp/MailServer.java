package chatapp;

import com.sun.mail.util.logging.MailHandler;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import java.io.Serializable;

public class MailServer {

    private String hostname = "mailServer";
    private int port = 252;
    private SMTPServer smtpServer;

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MailServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        initDatabaseConnection();
    }

    public void initDatabaseConnection() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("chatapp");
    }

    public void saveEmail(String sender, StringBuilder emailData) {
        Map<String, Object> email = new HashMap<>();
        email.put("sender", sender);
        email.put("emailData", emailData.toString());
        database.getCollection("emails").insertOne(new org.bson.Document(email));
    }

    public void run() {
        smtpServer = new SMTPServer(new MailHandlerFactory() {
            @Override
            public MessageHandler create(MessageContext ctx) {
                return new MessageHandler() {
                    String from;
                    String body;
                    String subject;
                    StringBuilder emailData = new StringBuilder();

                    @Override
                    public void from(String sender) throws RejectException {
                        from = sender;

                    }

                    @Override
                    public void recipient(String s) throws RejectException {

                    }

                    @Override
                    public void data(InputStream inputStream)
                            throws RejectException, TooMuchDataException, IOException {
                        System.out.println("Received a message");

                        // read the email data
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            emailData.append(line);
                        }
                    }

                    @Override
                    public void done() {
                        System.out.println("Email received");
                        saveEmail(from, emailData);
                    }
                };
            }
        }

        );
        boolean isRunning = false;
        smtpServer.setHostName(hostname);
        smtpServer.setPort(port);
        while (!isRunning) {
            try {
                smtpServer.start();
                isRunning = true;
                System.out.println("[Mail Server] started on port " + port);
            } catch (Exception e) {
                System.out.println("[Mail Sever] port already used , trying different part ");
                port++;
            }
        }

    }

    public static void main(String[] args) {
        MailServer mailServer = new MailServer("mailServer", 252);
        mailServer.run();
    }

}
