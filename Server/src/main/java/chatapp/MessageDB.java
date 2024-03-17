package chatapp;

import java.io.Serializable;

public class MessageDB implements Serializable , Comparable<MessageDB>{
    private int priority;
    private String content;

    public MessageDB(int priority, String content) {
        this.priority = priority;
        this.content = content;

    }
    @Override
    public int compareTo(MessageDB message) {
        return Integer.compare(this.priority, message.priority);
    }

    public int getPriority() {
        return priority;
    }

    public String getContent() {
        return content;
    }
}