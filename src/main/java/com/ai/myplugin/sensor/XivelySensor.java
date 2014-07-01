package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.services.xively.XivelyService;
import com.ai.myplugin.util.SensorResultBuilder;
import com.ai.myplugin.util.conf.Config;
import com.ai.myplugin.util.conf.Configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Map;
import java.util.Set;

@PluginImplementation
@PluginHeader(version = "0.0.1", author = "Francis", category = "Net", iconURL = "http://app.waylay.io/icons/xively.png")
public class XivelySensor implements SensorPlugin {

    public static final String PROPERTY_XIVELY_API_KEY = "apiKey";
    public static final String SETTING_XIVELY_KEY = "xivelyKey";

    private String apiKey;

    private XivelyService xivelyService;

    @Override
    public String getName() {
        return "Xively";
    }

    @Override
    public void setup(SessionContext sessionContext){
        Configuration configuration = Config.load();
        this.apiKey = configuration.getNonEmptyString(SETTING_XIVELY_KEY);
    }

    @Override
    public void shutdown(SessionContext sessionContext) {

    }

    @Override
    public SensorResult execute(SessionContext sessionContext) {
        if(xivelyService == null){
            xivelyService = new XivelyService(apiKey);
        }
        //Feed feed = XivelyService.instance().feed().get(123);
        //JsonObject feed = xivelyService.api().readFeed(597012994);
        JsonObject stream = xivelyService.api().readDatastream(597012994, "testsensor");
        //xivelyService.api().readFeeds();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return SensorResultBuilder
                .success()
                .withRawData(gson.toJson(stream))
                .build();
    }

    @Override
    public Set<String> getSupportedStates() {
        return null;
    }

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        return null;
    }

    @Override
    public void setProperty(String property, Object value) {
        switch (property) {
            case PROPERTY_XIVELY_API_KEY:
                this.apiKey = value.toString();
                break;
            default:
                throw new RuntimeException("Unknown property: " + property);
        }
    }

    @Override
    public Object getProperty(String s) {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

}
