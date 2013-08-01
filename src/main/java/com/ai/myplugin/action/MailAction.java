package com.ai.myplugin.action;

import com.ai.bayes.plugins.BNActionPlugin;
import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@PluginImplementation
public class MailAction implements BNActionPlugin {
    private static String CONFIG_FILE = "bn.properties";

    private static final String NAME = "Mail";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();
    private static final String MAIL_USER = "e-mail";
    private static final String MAIL_PASSWORD = "e-mailPassword";
    private static final String MAIL_TO = "address";
    private static final String MAIL_FROM = "e-mailFrom";
    private static final String MESSAGE = "message";
    private static final String SUBJECT = "subject";
    private String eMailAddress = "" ;
    private String message = "" ;
    private String subject = "" ;

    //in case that the file exist, use these properties
    public void fetchMailPropertiesFromFile() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(CONFIG_FILE));
        propertiesMap.put(MAIL_USER, properties.get(MAIL_USER));
        propertiesMap.put(MAIL_PASSWORD, properties.get(MAIL_PASSWORD));
        propertiesMap.put(MAIL_FROM, properties.get(MAIL_FROM));
    }



    @Override
    public String[] getRequiredProperties() {
        return new String[] {MAIL_TO, SUBJECT, MESSAGE};
    }

    @Override
    public void setProperty(String s, Object o) {
        if(MAIL_TO.equals(s))
            eMailAddress = (String) o;
        else if (MESSAGE.equals(s))
            message = (String) o;
        else if (SUBJECT.equals(s))
            subject = (String) o;
        else
            throw new RuntimeException("Property "+ s + " not in the required settings");
    }

    @Override
    public Object getProperty(String s) {
        if(MAIL_TO.equals(s))
            return eMailAddress;
        else if (MESSAGE.equals(s))
            return message;
        else if (SUBJECT.equals(s))
            return subject;
        else
            return null;
    }

    @Override
    public String getDescription() {
        return "Email action" ;
    }

    @Override
    public ActionResult action(TestSessionContext testSessionContext) {
        boolean success = true;
        try {
            fetchMailPropertiesFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        if(success){
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication((String)propertiesMap.get(MAIL_USER),
                                    (String)propertiesMap.get(MAIL_PASSWORD));
                        }
                    });

            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress((String) propertiesMap.get(MAIL_FROM)));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse((String) getProperty(MAIL_TO)));
                message.setSubject((String) getProperty(SUBJECT));

                Map attributes = testSessionContext.getAllAttributes();
                StringBuffer messageToAppend = new StringBuffer();
                messageToAppend.append("message: ").append(getProperty(MESSAGE)).append("\n\n");
                for(Object key :attributes.keySet()){
                    messageToAppend.append(key)
                            .append(": ")
                            .append(attributes.get(key))
                            .append("\n");
                }
                message.setText(messageToAppend.toString());
                Transport.send(message);

            } catch (MessagingException e) {
                e.printStackTrace();
                success = false;
            }

        }

        final boolean finalSuccess = success;
        return new ActionResult() {
            @Override
            public boolean isSuccess() {
                return finalSuccess;
            }

            @Override
            public String getObserverState() {
                return null;
            }
        } ;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void main(String[] args) {
        MailAction mail = new MailAction();
        mail.setProperty(MAIL_TO, "veselin.pizurica@gmail.com");
        mail.setProperty(SUBJECT, "test the action");
        mail.setProperty(MESSAGE, "hello vele");
        mail.action(null);

    }
}