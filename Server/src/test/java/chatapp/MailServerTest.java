package chatapp;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MailServerTest {

    private MongoDatabase database ;
    private MailServer mailServer;


    @BeforeEach
    public void setUp() {
        mailServer = new MailServer("localhost", 27017);
        database = mock(MongoDatabase.class);
        MongoCollection collection = mock(MongoCollection.class);
        Document document = Mockito.mock(Document.class);
        when(database.getCollection("emails")).thenReturn(collection);
        when(collection.insertOne(document)).thenReturn(null);
        Whitebox.setInternalState(mailServer, "database", database);
        System.out.println("database: " + database);

    }
    @Test
    void saveEmailTest() {


        String sender = "kariem";
        StringBuilder emailData = new StringBuilder("Email body");
        Map<String, Object> expectedEmail = new HashMap<>();
        expectedEmail.put("sender", sender);
        expectedEmail.put("emailData", emailData.toString());
        Document expectedDocument = new Document(expectedEmail);

        mailServer.saveEmail(sender, emailData);

        verify(database.getCollection("emails")).insertOne(eq(expectedDocument));




    }
}