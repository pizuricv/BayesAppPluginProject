package com.ai.myplugin.action;


import com.ai.api.ActuatorPlugin;
import com.ai.api.ActuatorResult;
import com.ai.api.SessionContext;
import com.ai.api.SessionParams;
import com.ai.myplugin.util.conf.Config;
import com.ai.myplugin.util.RawDataParser;
import com.ai.myplugin.util.conf.Configuration;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import java.util.HashMap;
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
public class MailAction implements ActuatorPlugin {
    private static final Log log = LogFactory.getLog(MailAction.class);

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

    /**
     * message context can be provided the testSessionContext, in the format "messageTemplate"
     * format "Hello world <{name:nodename, property:propertyname}>"
     * Example "Weather in Gent today is {node:Gent, property: wheather}"
     * will be formated as "weather in Gent today is Rain, if the value of the property is Rain
     *
     * @param testSessionContext context for the action
     */
    @Override
    public ActuatorResult action(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", action type:" +this.getClass().getName());
        boolean success = true;
        try {
            fetchMailPropertiesFromFile();
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
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


                Map map = (Map) testSessionContext.getAttribute(SessionParams.RAW_DATA);
                String messageString = (String) getProperty(MESSAGE);
                messageString = "message: "  + RawDataParser.parseTemplateFromRawMap(messageString, map);

                String explainReason = RawDataParser.giveTargetNodeStateAsString(testSessionContext);
                message.setText(messageString + explainReason);
                Transport.send(message);


            } catch (MessagingException e) {
                e.printStackTrace();
                log.error(e.getMessage());
                success = false;
            }
        }

        final boolean finalSuccess = success;
        return new ActuatorResult() {
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


    //in case that the file exist, use these properties
    private void fetchMailPropertiesFromFile(){
        Configuration config = Config.load();
        propertiesMap.put(MAIL_USER, config.getString(MAIL_USER));
        propertiesMap.put(MAIL_PASSWORD, config.getString(MAIL_PASSWORD));
        propertiesMap.put(MAIL_FROM, config.getString(MAIL_FROM));
    }


    public static void main(String[] args) {
        MailAction mail = new MailAction();
        mail.setProperty(MAIL_TO, "veselin.pizurica@gmail.com");
        mail.setProperty(SUBJECT, "test the action");
        mail.setProperty(MESSAGE, "hello vele node1->value1");

        SessionContext testSessionContext = new SessionContext(1);
        Map<String, Object> mapTestResult = new HashMap<String, Object>();
        JSONObject objRaw = new JSONObject();
        objRaw.put("value1", 1);
        objRaw.put("time", 123);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node1", objRaw);

        objRaw = new JSONObject();
        objRaw.put("value2", 1);
        objRaw.put("time", 213213);
        objRaw.put("rawData", objRaw.toJSONString());
        mapTestResult.put("node2", objRaw);

        testSessionContext.setAttribute(SessionParams.RAW_DATA, mapTestResult);

        mail.action(testSessionContext);
    }
}