/**
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.indiana.d2i.htrc.portal;

import org.markdownj.MarkdownProcessor;
import play.Logger;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Utils {
    private static Logger.ALogger log = play.Logger.of("application");

    public static String shortenTitle(String worksetName){
        if (worksetName.length() > 25){
            return worksetName.substring(0, 25) + "...";
        }

        return worksetName;
    }

    public static String markdownToHtml(String markdown) {
        MarkdownProcessor markdownProcessor = new MarkdownProcessor();
        return markdownProcessor.markdown(markdown);
    }

    public static void sendMail(String recipient, String subject, String body) {
        Properties smptProperties = getSMTPPropertiesForIUMailRelay();

        Session session = Session.getInstance(smptProperties,
                getPasswordAuthenticator(PlayConfWrapper.getHTRCEmailUserName(), PlayConfWrapper.getHTRCEmailPassword()));

        try {
            Transport.send(buildMessage(session, recipient, subject, body));
            log.info("Message with subject : " + subject + " is sent successfully.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Message buildMessage(Session session, String recipient, String subject, String body)
            throws MessagingException {
        Message message = new MimeMessage(session);

        // TODO: Load Portal's email address from configuration file
        message.setFrom(new InternetAddress("sharc@indiana.edu"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        return message;
    }

    private static Properties getSMTPPropertiesForIUMailRelay() {
        Properties props = new Properties();

        // TODO: Load these properties from configuration file
        props.put("mail.smtp.host", "mail-relay.iu.edu");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        return props;
    }

    private static Authenticator getPasswordAuthenticator(String userName, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
    }
}
