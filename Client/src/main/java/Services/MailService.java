package Services;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

public class MailService implements Runnable , Service{
    private String USER_NAME ;

    private String  PWD_CODE = "GEEEBIXZYSPFOTHS";
    private final String MAIL_HOST = "localhost";
    private String MAIL_SMTP_AUTH = "true";


    private String NOTIFICATION_MAIL_TITLE ;

    private String NOTIFICATION_MAIL_CONTENT ;


    public MailService(String USER_NAME, String NOTIFICATION_MAIL_TITLE, String NOTIFICATION_MAIL_CONTENT) {
        this.USER_NAME = USER_NAME;
        this.NOTIFICATION_MAIL_TITLE = NOTIFICATION_MAIL_TITLE;
        this.NOTIFICATION_MAIL_CONTENT = NOTIFICATION_MAIL_CONTENT;

    }

    public Session getMailSession(){
        Properties properties = new Properties();
        properties.setProperty("mail.host", MAIL_HOST);
        properties.setProperty("mail.smtp.auth", MAIL_SMTP_AUTH);
        properties.setProperty("mail.smtp.port", "252"); // Specify your port here

        Authenticator authenticator = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER_NAME,PWD_CODE);
            }
        };

        Session session = Session.getDefaultInstance(properties, authenticator);
        return session;
    }
    public  void sendMailTOSingleUser(String title, String content){
        Session session = getMailSession();
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(USER_NAME));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress("supportServer"));
            message.setRecipient(Message.RecipientType.CC, new InternetAddress("supportServer"));
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println("Error in sending mail to user");
//            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        System.out.println("Mail service starting");

    }

    @Override
    public void stop() {
        System.out.println("Mail service stopping");


    }

    public void run(){
        sendMailTOSingleUser(NOTIFICATION_MAIL_TITLE,NOTIFICATION_MAIL_CONTENT);
    }

    @Override
    public void dc() {


    }


}
