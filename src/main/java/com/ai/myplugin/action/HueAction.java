package com.ai.myplugin.action;

import com.ai.api.ActuatorPlugin;
import com.ai.api.PropertyType;
import com.ai.api.SessionContext;
import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.philips.lighting.model.PHHueError.*;
import static com.philips.lighting.hue.sdk.PHMessageType.*;

/**
 * For more info see
 * http://developers.meethue.com/
 * and
 * https://github.com/PhilipsHue/PhilipsHueSDK-Java-MultiPlatform-Android
 *
 * TODO we need a way to shut down the sdk, we need a lifecycle
 * TODO let user select bridge?
 * TODO let user select the light / group by name
 * TODO let user select the effect / color / brightness / ...
 *
 */
@PluginImplementation
public class HueAction implements ActuatorPlugin {

    private static final Logger log = LoggerFactory.getLogger(HueAction.class);

    private static final String NAME = "PhilipsHue";
    private static final String DEVICE_NAME = "Waylay";

    /**
     * It is recommended that a unique identifier for the device be used as the username
     * Also the length of this string should be long enough
     */
    private static final String DEVICE_USER = "Waylay" + HueAction.class.getSimpleName();

    private final PHHueSDK phHueSDK;
    private final WaylayPHSDKListener listener;


    public HueAction() {
        log.info("Starting " + this);
        this.phHueSDK = PHHueSDK.create();
        this.listener = new WaylayPHSDKListener(phHueSDK);

//        PHLog log  = (PHLog) phHueSDK.getSDKService(PHHueSDK.LOG);
//        log.setSdkLogLevel();

        // Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
        this.phHueSDK.setDeviceName(DEVICE_NAME);

        log.info("Hue SDK version: {}", phHueSDK.getSDKVersion());
        log.info("Hue device name: {}", phHueSDK.getDeviceName());


        // Register the PHHueListener to receive callback notifications on Bridge events.
        phHueSDK.getNotificationManager().registerSDKListener(listener);

        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        // Start the UPNP Searching of local bridges.
        sm.search(true, true);
    }

    @Override
    public void action(SessionContext sessionContext) {
        // in ace we are not connected here we might want to retry a connection?
        if(listener.isWaitingForAuthenticatonButton()){
            throw new RuntimeException("Please click the button on the bridge to let waylay connect to it");
        }

        PHBridge bridge = phHueSDK.getSelectedBridge();
        if(bridge != null){
            List<PHLight> lights = bridge.getResourceCache().getAllLights();
            for(PHLight light:lights){
                log.info("light found: " + light);
            }

            Optional<PHLight> first = lights.stream().findFirst();
            first.ifPresent( light -> {
                log.info("Setting first light to alert mode: {}", light.getName());
                // You don't need all the details in order
                // to actually modify a light. Just it's ID.
                // From my observations light IDs start at 1
                // final PHLight light = new PHLight("", "1", "", "");

                PHLightState state = new PHLightState();
                state.setAlertMode(PHLight.PHLightAlertMode.ALERT_LSELECT);
                //state.setEffectMode(PHLight.PHLightEffectMode.EFFECT_COLORLOOP);
                // Setting to 255 causes an exception to raise :(
                //state.setBrightness(254);
                //state.setHue(46920); // Make light blue
                //state.setOn(true);
                bridge.updateLightState(light, state);
            });
        }else{
            // maybe we should fail with an exception here?
            log.warn("No bridge selected");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        return new HashMap<>();
    }

    @Override
    public void setProperty(String s, Object o) {

    }

    @Override
    public Object getProperty(String s) {
        return null;
    }

    @Override
    public String getDescription() {
        return "Control Philips Hue light(s)";
    }

    // package private for testing
    void waitForConnection(long timeout, TimeUnit unit) throws InterruptedException {
        this.listener.awaitConnection(timeout, unit);
    }

    public void shutDown(){
        log.info("Shutting down " + this);
        this.phHueSDK.destroySDK();
    }

    private static class WaylayPHSDKListener implements PHSDKListener {

        private final PHHueSDK phHueSDK;

        private final AtomicBoolean waitingForAuthenticationButton = new AtomicBoolean();
        private final CountDownLatch latch = new CountDownLatch(1);

        private PHAccessPoint accessPoint;

        private WaylayPHSDKListener(final PHHueSDK phHueSDK) {
            this.phHueSDK = phHueSDK;
        }

        public boolean isWaitingForAuthenticatonButton(){
            return waitingForAuthenticationButton.get();
        }

        public void awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
            latch.await(timeout, unit);
        }

        @Override
        public void onCacheUpdated(int i, PHBridge phBridge) {

        }

        @Override
        public void onBridgeConnected(PHBridge phBridge) {
            log.info("Connected to bridge {}", phBridge);
            waitingForAuthenticationButton.set(false);
            latch.countDown();
            phHueSDK.setSelectedBridge(phBridge);
            phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            phHueSDK.getLastHeartbeat().put(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress(), System.currentTimeMillis());
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            // Start the Pushlink Authentication.
            this.waitingForAuthenticationButton.set(true);
            phHueSDK.startPushlinkAuthentication(accessPoint);
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> phAccessPoints) {
            log.info("Found {} access point(s)", phAccessPoints.size());
            for(PHAccessPoint accessPoint:phAccessPoints){
                String debugUrl = "http://" + accessPoint.getIpAddress() + "/debug/clip.html";
                log.info("\taccess point {} {} {}", accessPoint.getIpAddress(), accessPoint.getMacAddress(), debugUrl);
            }

            Optional<PHAccessPoint> first = phAccessPoints.stream().findFirst();
            first.ifPresent( ap -> {
                ap.setUsername(DEVICE_USER);
                log.info("Connecting to first access point {} with username {}", ap.getIpAddress(), ap.getUsername());
                this.accessPoint = ap;
                phHueSDK.connect(ap);
            });
        }

        @Override
        public void onError(int code, String message) {
            if (code == BRIDGE_NOT_RESPONDING) {
                log.error(message);
            }
            else if (code == PUSHLINK_BUTTON_NOT_PRESSED) {
                // can be used to update a progress bar
                log.error(message);
            }
            else if (code == PUSHLINK_AUTHENTICATION_FAILED) {
                // just ask again, we might want to limit the number of times we do this
                log.error(message);
                log.info("Retrying authentication...");
                phHueSDK.connect(accessPoint);
            }
            else if (code == BRIDGE_NOT_FOUND) {
                log.error(message);
            }
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {

        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {

        }
    }
}
