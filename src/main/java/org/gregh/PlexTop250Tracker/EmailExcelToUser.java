package org.gregh.PlexTop250Tracker;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailExcelToUser {
    private String destinationEmailAddress;
    private String senderEmailAddress;
    private WriteMovieTitlesToExcel writeMovieTitlesToExcel;
    private Logger logger;

    public EmailExcelToUser(WriteMovieTitlesToExcel writeMovieTitlesToExcel) {
        this.senderEmailAddress = "plexTop250Tracker@javamail.com";
        this.writeMovieTitlesToExcel = writeMovieTitlesToExcel;
        this.logger = LogManager.getLogger(EmailExcelToUser.class);
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
        input.close();

        sendEmail(getDestinationEmailAddress(), writeMovieTitlesToExcel.getFileOutName());
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
        
        input.close();
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
            System.out.println("There was an error with an address");
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            System.out.println("There was an error with authentication.");
            e.printStackTrace();
        } catch (MessagingException e) {
            System.out.println("There was an error with messaging");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("There was an error with sending the email. Please check the log");
            e.printStackTrace();
        }
    }
}
