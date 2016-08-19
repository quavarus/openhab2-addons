package org.openhab.binding.smartthings.client;

import java.io.IOException;
import java.util.List;

import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.client.model.Endpoint;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmartThingsService {

    private final String clientId;
    private final String authToken;

    private final OkHttpClient httpClient;

    public SmartThingsService(String clientId, String authToken) {
        this.authToken = authToken;
        this.clientId = clientId;
        this.httpClient = initHttp();
    }

    private OkHttpClient initHttp() {
        return new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + authToken)
                        .build();
                return chain.proceed(request);
            }
        }).build();
    }

    private SmartThingsAPI getSmartThingsApi() {
        String endpointUrl = "https://graph.api.smartthings.com/api/";
        Retrofit retrofit = new Retrofit.Builder().baseUrl(endpointUrl).client(httpClient)
                .addConverterFactory(GsonConverterFactory.create()).build();
        SmartThingsAPI service = retrofit.create(SmartThingsAPI.class);
        return service;
    }

    private <T> T executeCall(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                T value = response.body();
                return value;
            } else {
                throw new SmartThingsClientException(response.errorBody().string());
            }

        } catch (Exception e) {
            throw new SmartThingsClientException("Error executing api call.", e);
        }
    }

    private SmartAppApi getSmartAppApi() {
        SmartThingsAPI api = getSmartThingsApi();
        List<Endpoint> endpoints = executeCall(api.getEndpoints(clientId));
        if (endpoints != null && endpoints.size() > 0) {
            String baseUrl = endpoints.get(0).getUri().toString() + "/";
            Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create()).build();
            SmartAppApi service = retrofit.create(SmartAppApi.class);
            return service;
        } else {
            throw new SmartThingsClientException("No Endpoints Authorized");
        }

    }

    public List<Device> getDevices() {
        return executeCall(getSmartAppApi().getDevices());
    }

    public Device getDevice(String deviceId) {
        Device device = executeCall(getSmartAppApi().getDevice(deviceId));
        if (device == null) {
            throw new SmartThingsClientException("Device Not Found");
        }
        return device;
    }

    public Device runDeviceCommand(String deviceId, String command, String... arugments) {
        return executeCall(getSmartAppApi().runDeviceCommand(deviceId, command));
    }

}
