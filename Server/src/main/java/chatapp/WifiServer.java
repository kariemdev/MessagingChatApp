package chatapp;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import org.bson.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class WifiServer implements Runnable {
    private ServerSocket serverSocket;
    private Map<String, Socket> activeConnections; // keeps track of all active connections
    private Map<String, Socket> allConnections; // keeps track of all connections
    private Map<String , PriorityQueue<MessageDB>> messageQueue;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public WifiServer() {

        // keeps track of all active connections
        activeConnections = new HashMap<>();
        messageQueue = new HashMap<>();
        allConnections = new HashMap<>();

//        databaseInit();
//        loadMessageQueue();
//        serverInit();

    }
    public void serverInit() {
        try {
            serverSocket = new ServerSocket(8082);
            System.out.println("Server started");
        } catch (Exception e) {
            System.out.println("Error in opening server socket");
//            e.printStackTrace();
        }
    }
    public void databaseInit() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("chatapp");
        database.createCollection("messages");
        System.out.println("Database created");
    }
    public void connectionNotifier(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run(){
                System.out.println( "Users logged into the server ");
                for (Map.Entry<String , Socket> entry : activeConnections.entrySet()){
                    System.out.println(entry.getKey());
                }

                //print queues and the details of thw queues
                for (Map.Entry<String , PriorityQueue<MessageDB>> entry : messageQueue.entrySet()){
                    System.out.println("User: " + entry.getKey() + " -> " + entry.getValue());
                    for (MessageDB message : entry.getValue()){
                        System.out.println("Priority: " + message.getPriority() + " Content: " + message.getContent());
                    }
                    System.out.println("End of queue");
                }
            }

        }, 0, 10000);
    }
    public void run() {
        databaseInit();
        loadMessageQueue();
        serverInit();
        try {
            connectionNotifier(); // prints the users logged into the server every 1 second
            while (true) {
                if (serverSocket != null) {
                    handleClient();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in server client accept method");
//            e.printStackTrace();
        }
    }
    public void saveMessageQueue() {
        // Create a new collection for the message queue
        var collection = database.getCollection("messageQueue");
        collection.drop(); // Clear the existing data

        // Convert the messageQueue map to a format that MongoDB can store
        for (Map.Entry<String, PriorityQueue<MessageDB>> entry : messageQueue.entrySet()) {
            List<DBObject> messages = new ArrayList<>();
            PriorityQueue<MessageDB> queue = entry.getValue();
            for (MessageDB message : queue) {
                DBObject messageObject = new BasicDBObject();
                messageObject.put("priority", message.getPriority());
                messageObject.put("content", message.getContent());
                messages.add(messageObject);
            }

            // Create a document for each user's message queue
            DBObject userMessages = new BasicDBObject();
            userMessages.put("user", entry.getKey());
            userMessages.put("messages", messages);

            // Insert the document into the collection
            collection.insertOne(new org.bson.Document(userMessages.toMap()));
        }
    }
    public void loadMessageQueue() {
        // Get the collection for the message queue
        var collection = database.getCollection("messageQueue");

        // Clear the existing messageQueue map
        messageQueue.clear();

        // Load the messageQueue map from MongoDB
        for (Document document : collection.find()) {
            String user = document.getString("user");
            List<Document> messages = (List<Document>) document.get("messages");
            PriorityQueue<MessageDB> queue = new PriorityQueue<>(Comparator.comparingInt(MessageDB::getPriority));
            for (Document messageDocument : messages) {
                int priority = messageDocument.getInteger("priority");
                String content = messageDocument.getString("content");
                queue.add(new MessageDB(priority, content));
            }
            messageQueue.put(user, queue);
        }
    }
    public void handleClient() throws IOException {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected");
        Thread handler = new Thread(new ClientHandler(clientSocket , database , activeConnections, allConnections , messageQueue));
        handler.start();
    }
    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter outSender;
        private PrintWriter outReceiver;
        private MongoDatabase database;
        private String clientName;

        private Map<String, Socket> map;
        private Map<String, Socket> allConnections;
        private Map<String , PriorityQueue<MessageDB>> messageQueue;

        public ClientHandler(Socket clientSocket , MongoDatabase database ,  Map<String, Socket> map , Map<String,Socket>allConnections,  Map<String , PriorityQueue<MessageDB>> messageQueue) {
            this.clientSocket = clientSocket;
            this.database = database;
            this.map = map;
            this.messageQueue = messageQueue;
            this.allConnections = allConnections;
        }

        public void clientHandlerInit(Socket clientSocket ) {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outSender = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                // e.printStackTrace();
                System.err.println("Error in opening stream");
            }
        }

        public void commandParser(String name , String input){
            String[] command = input.split(" " , 3 );
            String commandName = command[0];
            String receiver = command.length > 1 ? command[1] : "";
            String msg  = command.length > 2 ? command[2] : "";


            switch (commandName){
                case "/help":
                    outSender.println("List of commands: \n /message <receiver> <message> : sends a message to the receiver " +
//                            "\n /exit : exits the chat " +
                            "\n /allMsgs : gets all messages received" +
                            "\n /notify <receiver> <message> : sends a notification(Priority level 2 msg) to the receiver" +
                            "\n /alert <receiver> <message> : sends an alert(Priority level 1 msg ) to the receiver" +
                            "\n /checkNotifications : checks for notifications" +
                            "\n /checkUnseen : checks for unseen messages" );
                    break;
                case "/msg":
                    handleMessages(name, receiver, msg , 3 );
                    break;
                case "/alert":
                    handleMessages(name, receiver, msg , 1 );
                    break;
                case "/notify":
                    handleMessages(name, receiver, msg , 2);
                    break;
                case "/allMsgs":
                    getMessagesReceived(name);
                    break;
                case "/checkNotifications":
                    handleLevel2Messages(name);
                    break;
                case "/checkUnseen":
                    handleLevel3Messages(name);
                    break;
                default:
                    outSender.println("Invalid command");
                    break;

            }
        }
        public void handleMessages(String name, String receiver, String msg , int priority) {
            try {
                if (receiver.equals("")){
                    outSender.println("Invalid command , must be /<message,notify,alarm> <receiver> <message>");
                }
                else {
                messagingService(name, receiver, msg , priority);
                }
            } catch (IOException e) {
                System.out.println("Error in messaging service");
//                e.printStackTrace();

            }
        }
        public void handleLevel2Messages(String name) {
            if (messageQueue.get(name).isEmpty()){
                outSender.println("No notifications");
            }
            else {
                ArrayList<MessageDB> messagesToRemove = new ArrayList<>();
                for (MessageDB message : messageQueue.get(name)){
                    if (message.getPriority() == 2){
                        outSender.println("[Notification] -> " + message.getContent());
                        //removes msg from queue
                        messagesToRemove.add(message);

                    }
                }
                //removes messages from queue and updates the db
                synchronized (messageQueue.get(name)){
                for (MessageDB message : messagesToRemove){
                    messageQueue.get(name).remove(message);
                }
                saveMessageQueue();
                }

            }
        }
        public  void handleLevel3Messages(String name) {
            if (messageQueue.get(name).isEmpty()){
                outSender.println("No unseen messages");
            }
            else {
                ArrayList<MessageDB> messagesToRemove = new ArrayList<>();
                boolean found = false;
                for (MessageDB message : messageQueue.get(name)){
                    if (message.getPriority() == 3){
                        found = true;
                        outSender.println("[Unseen] -> " + message.getContent());
                        //removes msg from queue
                        messagesToRemove.add(message);
                    }
                }
                //removes messages from queue and updates the db
                synchronized (messageQueue.get(name)) {
                    for (MessageDB message : messagesToRemove) {
                        messageQueue.get(name).remove(message);
                    }
                    saveMessageQueue();
                }
                if (!found){
                    outSender.println("No unseen messages");
                }
            }
        }
        public void storeMessage(String message , String sender , String receiver){
            database.getCollection("messages").insertOne(new Document("message" , message).append("sender" , sender).append("receiver" , receiver).append("time" , System.currentTimeMillis()));
            System.out.println("Message stored from " + sender + " to " + receiver + " : " + message);
        }
        public void getMessagesReceived(String receiver){
            for (Document doc : database.getCollection("messages").find(new Document("receiver" , receiver))){
                String sender = doc.getString("sender");
                String message = doc.getString("message");
                outSender.println("From " + sender + " : " + message);
            }


        }
        
        public void run() {
            clientHandlerInit(clientSocket);
            try {

                // get the name of the client
                outSender.println("Welcome to the server ,  Please enter your name:");
                clientName = in.readLine();
                allConnections.put(clientName , clientSocket);

                //checks if user exists in db
                if (database.getCollection("users").find(new Document("user" , clientName)).first() != null){
                    outSender.println("Welcome back " + clientName + " , !help for more info and commands available");
                }
                else {
                    outSender.println("Welcome to the server  " + clientName + " !, !help for more info and commands available");
                    //saves user in db
                    database.getCollection("users").insertOne(new Document("user" , clientName));
                    //creates queue for user
                    synchronized (messageQueue) {
                        messageQueue.put(clientName, new PriorityQueue<>());
                        saveMessageQueue();
                    }

                }

                //puts user in active connections
                map.put(clientName, clientSocket);


                System.out.println("Client name: " + clientName + " -> connected");

                // handles sent messages while offline and notifies user
                userQueueHandling(clientName);



                // start the messaging service
                commandHandler(clientName);

            } catch (IOException e) {
                System.out.println("Error in message reading (Client disconnected)");
//                e.printStackTrace();
            }finally {
                try {
                    //remove name from active connections when client disconnencts
                    map.remove(clientName);
                    closeResources();
                } catch (IOException e) {
                    System.out.println("Error in closing resources");
//                    e.printStackTrace();
                }
            }
        }

        public synchronized void userQueueHandling(String name) {
            if (!messageQueue.get(name).isEmpty()){
                //gets only high priority messages (priority 1) and notifies
                ArrayList<MessageDB> messagesToRemove = new ArrayList<>();
                for (MessageDB message : messageQueue.get(name)){
                    System.out.println("Message: " + message.getContent() +"ENTTTERED");
                    if (message.getPriority() == 1){
                        outSender.println("[ALERT!] -> " + message.getContent());;
                        messagesToRemove.add(message);
                    }
                }
                for (MessageDB message : messagesToRemove){
                    messageQueue.get(name).remove(message);
                }
                //gets only mid priority messages (priority 2)
                for(MessageDB message : messageQueue.get(name)){
                    if (message.getPriority() == 2){
                        outSender.println("You have important msgs (priority level 2) , do /checkNotifications to see them! : " );
                        break;
                    }
                }
                //gets only low priority messages (priority 1)
                for (MessageDB message : messageQueue.get(name)){
                    if (message.getPriority() == 3){
                        outSender.println("You have unread msgs (priority level 1) , do /checkUnseen to see them! : " );
                        break;

                    }
                }
            }
        }

        public synchronized void messagingService(String sender , String receiver , String msg , int priority ) throws IOException {

            //checks db if the user exists
            if (database.getCollection("users").find(new Document("user" , receiver)).first() != null){
                outSender.println("User found , sending message");

                MessageDB message = new MessageDB(priority , "From " + sender + " : " + msg);
                if (map.get(receiver)== null){
                    sendMessageOffline(sender, receiver, msg, message);
                }
                else {
                    System.out.println("Sending message online");
                    sendMessageOnline(sender, receiver, msg);
                    storeMessage(msg, sender, receiver);
                }
            } else {
                    outSender.println("User not found , Please try again with an existing user : ");
            }

        }

        public void sendMessageOnline(String sender, String receiver, String msg) throws IOException {

            outReceiver = new PrintWriter(map.get(receiver).getOutputStream(), true);
            outReceiver.println("From " + sender + " : " + msg);

        }

        public void sendMessageOffline(String sender, String receiver, String msg, MessageDB message) {
            messageQueue.get(receiver).add(message);
            saveMessageQueue();
            storeMessage(msg, sender, receiver);
        }

        public void commandHandler(String name ){
            String command;
            try {
                while ((command = in.readLine()) != null) {
                    if (command.startsWith("/") && command.length() > 1) {
                        commandParser(name , command);
                    } else {
                        outSender.println("Invalid input , must be a command starting with /command args1 args2 . Do /help for more info ");
                    }
                }

            } catch (IOException e) {
                System.out.println("Error in command handling , (Client disconnected)");
                //removes client from active connections
                if (clientName != null)
                    map.remove(clientName);
//                e.printStackTrace();
            }
        }

        public void closeResources() throws IOException {
            in.close();
            outSender.close();
            if (outReceiver != null)
                outReceiver.close();
            clientSocket.close();
        }


    }




    public static void main(String[] args) {
        WifiServer server = new WifiServer();
        Thread serverThread = new Thread(server);
        serverThread.start();



    }
}
