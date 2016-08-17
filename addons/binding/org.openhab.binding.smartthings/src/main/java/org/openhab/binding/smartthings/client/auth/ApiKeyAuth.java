package org.openhab.binding.smartthings.client.auth;

import java.io.IOException;
import java.net.URI;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyAuth implements Interceptor {
    private final String location;
    private final String paramName;

    private String apiKey;

    public ApiKeyAuth(String location, String paramName) {
        this.location = location;
        this.paramName = paramName;
    }

    public String getLocation() {
        return location;
    }

    public String getParamName() {
        return paramName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String paramValue;
        Request request = chain.request();

        if (location == "query") {
            String newQuery = request.url().query();
            paramValue = paramName + "=" + apiKey;
            if (newQuery == null) {
                newQuery = paramValue;
            } else {
                newQuery += "&" + paramValue;
            }

            URI newUri;
            // try {
            newUri = request.url().uri();
            // } catch (URISyntaxException e) {
            // throw new IOException(e);
            // }

            request = request.newBuilder().url(newUri.toURL()).build();
        } else if (location == "header") {
            request = request.newBuilder().addHeader(paramName, apiKey).build();
        }
        return chain.proceed(request);
    }
}