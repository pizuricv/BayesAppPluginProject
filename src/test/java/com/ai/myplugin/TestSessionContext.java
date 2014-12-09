package com.ai.myplugin;

import com.ai.api.SessionContext;
import org.junit.Ignore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Ignore
public class TestSessionContext implements SessionContext{

    private final Map<String,Object> ctx = new ConcurrentHashMap<>();

    @Override
    public Long getId() {
        return 1L;
    }

    @Override
    public Map<String, Object> getAllAttributes() {
        // defensice copy
        return new HashMap<>(ctx);
    }

    @Override
    public Object getAttribute(String key, Object def) {
        Object value = ctx.get(key);
        if(value == null){
            return def;
        }
        return value;
    }

    @Override
    public Object getAttribute(String key) {
        return ctx.get(key);
    }

    @Override
    public String parseTemplateFromRawMap(String s, Map map) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setAttribute(String key, double value) {
        ctx.put(key, value);
    }
}
