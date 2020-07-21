package org.gregh.PlexTop250Tracker;

import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Scanner;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailExcelToUser {
    private String destinationEmailAddress;
    private String senderEmailAddress;
    private WriteMovieTitlesToExcel writeMovieTitlesToExcel;

    public EmailExcelToUser(WriteMovieTitlesToExcel writeMovieTitlesToExcel) {
        this.senderEmailAddress = "plexTop250Tracker@javamail.com";
        this.writeMovieTitlesToExcel = writeMovieTitlesToExcel;
    }

    public void setDestinationEmailAddress(String destinationEmailAddress) {
        this.destinationEmailAddress = destinationEmailAddress;
    }

    public String getDestinationEmailAddress() {
        return destinationEmailAddress;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
    }

    public void askUserForSenderEmail() {
        Scanner input = new Scanner(System.in);

        System.out.println("What email would you like to send the movie list to?");
        setDestinationEmailAddress(input.nextLine());

        sendEmail(getDestinationEmailAddress(), writeMovieTitlesToExcel.getFileOutName());
    }

    private void startLocalSMTPServer() {
        SimpleMessageListenerImpl simpleMessageListener = new SimpleMessageListenerImpl();
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(simpleMessageListener));
        smtpServer.setPort(25000);
        smtpServer.start();

        Wiser wiser = new Wiser();
        wiser.setPort(25001);
        wiser.start();
    }

    private Session connectToLocalSMTPServer() {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "localhost");
        properties.setProperty("mail.smtp.port", "25000");

        Session session = Session.getInstance(properties);

        return session;
    }

    private Session connectToGmailSMTPServer() {
        Scanner input = new Scanner(System.in);

        // Take in the users username and password for Gmail
        System.out.println("What is your username for gmail?");
        String username = input.nextLine();
        // User needs to let less secure apps through in order to login
        System.out.println("What is your password for gmail?");
        String password = input.nextLine();

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        return session;
    }

    private void sendEmail(String destinationEmailAddress, String fileName) {
        // Create a connection to the Gmail SMTP server
        Session session = connectToGmailSMTPServer();

        try {
            // Create a new email message
            MimeMessage emailMessage = new MimeMessage(session);

            // Set the basic aspects of the email message
            emailMessage.setFrom(new InternetAddress(senderEmailAddress));

            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(destinationEmailAddress));

            emailMessage.setSubject("Missing movies as of: " + LocalDateTime.now());

            // Add the excel file to the message
            Multipart messageMultiPart = new MimeMultipart();

            BodyPart messageBodyPart = new MimeBodyPart();

            // Needs the path to the file that you want to send
            DataSource excelFile = new FileDataSource("./" + fileName);
            messageBodyPart.setDataHandler(new DataHandler(excelFile));
            messageBodyPart.setFileName(fileName);
            messageMultiPart.addBodyPart(messageBodyPart);

            emailMessage.setContent(messageMultiPart);

            // Send the email message
            Transport.send(emailMessage);

            System.out.println("Message sent successfully");

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
