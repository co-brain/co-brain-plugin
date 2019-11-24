package de.lrz.gitlab.editorLogic;

import javax.mail.internet.MimeMessage;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

public class EMailHandler implements MessageHandler {

    public boolean handleTicket(Ticket ticket) {
        String content = "Dear " + ticket.responsiblePersonName + ",\n\n"
                + ticket.developingPersonName + " sent you the following message to the Code you wrote: \n"
                + "\"" + ticket.messageContent + "\"\n\n" + "You can find the corresponding Code Snippet here: \n"
                + ticket.gitLink + "\n\n Here you can contact " + ticket.developingPersonName
                + ": " + ticket.developingPersonMail;

        String subject = "Documentation Request, ID: " + ticket.ticketId;

        try {
            writingEMail(ticket.responsiblePersonMail, subject, content);
        } catch (MessagingException me) {
            System.out.println("Fehler beim Senden der Mail 1!");
            return false;
        }

        content = "You wrote to " + ticket.responsiblePersonName + " (" + ticket.responsiblePersonMail + "): \n\n" + content; 
        subject = "COPY: " + subject;

        try {
            writingEMail(ticket.developingPersonMail, subject, content);
        } catch (MessagingException me) {
            System.out.println("Fehler beim Senden der Mail!");
            return false;
        }

        return true;

    }

    private void writingEMail(String eMailAdresse, String subject, String content)
            throws MessagingException {
        String sender = "xxx.co-brain@xxxx.xxx";
        String password = "xxxxxxxxxxxxxx";
       String receiver = eMailAdresse;

        System.out.println(receiver);
        Properties properties = new Properties();

        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", "mail.gmx.net");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.user", sender);
        properties.put("mail.smtp.password", password);
        properties.put("mail.smtp.starttls.enable", "true");

        Session mailSession = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("mail.smtp.user"),
                        properties.getProperty("mail.smtp.password"));
            }
        });

        Message message = new MimeMessage(mailSession);
        InternetAddress addressTo = new InternetAddress(receiver);
        message.setRecipient(Message.RecipientType.TO, addressTo);
        message.setFrom(new InternetAddress(sender));
        message.setSubject(subject);
        message.setContent(content, "text/plain");
        Transport.send(message);
    }
}
