package com.rethink.mailappnew.controller.mail;


import android.util.Log;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class GmailUtil {
    private static final String TAG = "MailApp";

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       Email address of the receiver.
     * @param from     Email address of the sender, the mailbox account.
     * @param subject  Subject of the email.
     * @param bodyText Body text of the email.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */

    public static MimeMessage createEmail(String from, String to, String bcc, String subject,
                                          String bodyText) throws MessagingException {

        showLog("GmailUtils - createEmail()");

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        if (!bcc.equals("")) {
            email.addRecipient(javax.mail.Message.RecipientType.BCC,
                    new InternetAddress(bcc));
        }
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }


    public static MimeMessage createEmailWithAttachment(String from, String to, String bcc, String subject,
                                                        String bodyText, String filePath, String fileName) throws MessagingException {

        Log.d("FIlepath", filePath);

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        if (!bcc.equals("")) {
            email.addRecipient(javax.mail.Message.RecipientType.BCC,
                    new InternetAddress(bcc));
        }
        email.setSubject(subject);
        email.setText(bodyText);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filePath);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);

        email.setContent(multipart);

        return email;
    }


    /**
     * Create a Message from an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64 encoded email.
     * @throws IOException
     * @throws MessagingException
     */
    public static Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {

        showLog("GmailUtils - ccreateMessageWithEmail()");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.writeTo(baos);
        String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param service Authorized Gmail API instance.
     * @param userId  User's email address. The special value "me"
     *                can be used to indicate the authenticated user.
     * @param email   Email to be sent.
     * @throws MessagingException
     * @throws java.io.IOException
     */
    public static void sendMessage(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException {
        showLog("GmailUtils - sendMessage()");

        Message message = createMessageWithEmail(email);
        message = service.users().messages().send(userId, message).execute();

        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
    }

    /**
     * Get Message with given ID.
     *
     * @param service   Authorized Gmail API instance.
     * @param userId    User's email address. The special value "me"
     *                  can be used to indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return Message Retrieved Message.
     * @throws java.io.IOException
     */
    public static Message getMessage(Gmail service, String userId, String messageId, String format) throws IOException {
        Message message = null;
        if (format != null && !format.isEmpty()) {
            message = service.users().messages().get(userId, messageId).setFormat(format).execute();
        } else {
            message = service.users().messages().get(userId, messageId).execute();
        }
        System.out.println("Message snippet: " + message.getSnippet());
        System.out.println("-----------------------------------------------****");
        System.out.println("Message raw: " + Util.base64UrlDecode(message.getRaw()));
        System.out.println("Message raw Base64 android: " + Base64.encodeBase64URLSafeString(message.decodeRaw()));
        System.out.println("-----------------------------------------------****");
        return message;
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }
}