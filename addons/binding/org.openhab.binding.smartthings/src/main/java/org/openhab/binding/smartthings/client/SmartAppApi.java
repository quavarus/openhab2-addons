package org.openhab.binding.smartthings.client;

import java.util.List;

import org.openhab.binding.smartthings.client.model.App;
import org.openhab.binding.smartthings.client.model.Device;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SmartAppApi {

    @GET(" ")
    Call<App> getApp();

    @GET("devices/")
    Call<List<Device>> getDevices();

    @GET("device/{deviceId}")
    Call<Device> getDevice(@Path("deviceId") String deviceId);

    @PUT("device/{deviceId}/{command}")
    Call<Device> runDeviceCommand(@Path("deviceId") String deviceId, @Path("command") String command);
}
