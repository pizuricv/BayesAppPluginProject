package com.ai.myplugin.util;

import org.slf4j.Logger;
import retrofit.RestAdapter;

public class RetrofitSlf4jLog implements RestAdapter.Log{

    private final Logger logger;

    public RetrofitSlf4jLog(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }
}
