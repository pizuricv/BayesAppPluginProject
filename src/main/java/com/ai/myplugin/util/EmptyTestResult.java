/**
 * Created by User: veselin
 * On Date: 26/10/13
 */

package com.ai.myplugin.util;

import com.ai.api.SensorResult;

import java.util.List;
import java.util.Map;

public class EmptyTestResult implements SensorResult {
    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getName() {
        return "Empty Result";
    }

    @Override
    public String getObserverState() {
        return null;
    }

    @Override
    public List<Map<String, Number>> getObserverStates() {
        return null;
    }

    @Override
    public String getRawData() {
        return null;
    }
}
