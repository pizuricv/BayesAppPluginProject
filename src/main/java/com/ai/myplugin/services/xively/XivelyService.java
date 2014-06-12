package com.ai.myplugin.services.xively;

import com.ai.myplugin.util.RetrofitSlf4jLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

/**
 * TODO separate and release as oss project, both direct and async + reuse in andoid app?
 */
public class XivelyService {

    private static final Logger log = LoggerFactory.getLogger(XivelyService.class);

    private static final String ENDPOINT = "https://api.xively.com/v2";

    private final XivelyApiV2 api;
    private final Gson gson;

    public XivelyService(final String apiKey){

        this.gson = new GsonBuilder()
                //.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                //.registerTypeAdapter(Scenario.class, new ScenarioAdapter())
                .create();

        this.api = createRestClient(apiKey);
    }

    public XivelyApiV2 api(){
        return api;
    }

    private XivelyApiV2 createRestClient(final String apiKey) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setRequestInterceptor(new XivelyAuthRequestInterceptor(apiKey))
                //.setProfiler(new RequestLoggingProfiler(TAG))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setLog(new RetrofitSlf4jLog(log))
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new XivelyErrorHandler())
                .build();
        return restAdapter.create(XivelyApiV2.class);
    }

    private static class XivelyAuthRequestInterceptor implements RequestInterceptor {

        private final String apiKey;

        private XivelyAuthRequestInterceptor(final String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("X-ApiKey", apiKey);
        }
    }

    private static class XivelyErrorHandler implements ErrorHandler {
        @Override public Throwable handleError(RetrofitError cause) {
            //Response r = cause.getResponse();
            XivelyError error = (XivelyError) cause.getBodyAs(XivelyError.class);
            if(error != null){
                return new RuntimeException(error.title + " - " + error.errors, cause);
            }
//            if (r != null && r.getStatus() == 401) {
//                return new UnauthorizedException(cause);
//            }
            return cause;
        }
    }

}
