package chatapp;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class WifiServiceTest {
    private WifiServer wifiServer;
    private WifiServer.ClientHandler clientHandler;
    private Socket socketReciever;
    private Socket socketSender;
    private OutputStream outputStream;
    private MongoDatabase database;
    private MongoCollection collection;
    private Document document;
    private Map<String , Socket> map;

    private String receiver = "liu";

    private PrintWriter outReceiver;
    private PrintWriter outSender;





    @BeforeEach
    public void setUp() throws Exception {

        map = new HashMap<>();
        //mocks
        socketReciever = mock(Socket.class);
        socketSender = mock(Socket.class);
        outputStream = mock(OutputStream.class);
        database = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);
        document = Mockito.mock(Document.class);
        outReceiver = mock(PrintWriter.class);
        outSender = mock(PrintWriter.class);
        FindIterable<Document> mockIterable = mock(FindIterable.class);

        map.put(receiver, socketReciever);





        //behaviour
        when(socketReciever.getOutputStream()).thenReturn(outputStream);
        when(database.getCollection("users")).thenReturn(collection);
        when(database.getCollection("messages")).thenReturn(collection);
        when(collection.find(document)).thenReturn(mockIterable);
        when(collection.find(any(Document.class))).thenReturn(mockIterable);
        when(mockIterable.first()).thenReturn(new Document());


        when(collection.insertOne(document)).thenReturn(null);
        whenNew(PrintWriter.class).withParameterTypes(OutputStream.class, boolean.class).withArguments(any(OutputStream.class), eq(true)).thenReturn(outReceiver);






    }

    @Test
     void sendMessageTestOnlineTest() throws IOException {
        wifiServer = new WifiServer();
        Map<String, Socket> allConnections = mock(Map.class);
        Map<String, PriorityQueue<MessageDB>> messagingQueue = mock(Map.class);
         clientHandler = wifiServer.new ClientHandler(socketSender , database , map , allConnections , messagingQueue);
        Whitebox.setInternalState(clientHandler, "outSender", outSender);
        Whitebox.setInternalState(clientHandler, "map", map);
        Whitebox.setInternalState(clientHandler, "outReceiver", outReceiver);



        String sender = "kariem";
        String receiver = "liu";
        String message = "Hello liu ";
        clientHandler.messagingService(sender, receiver, message , 1 );

        verify(outSender).println(anyString());

    }

}
