package org.openhab.binding.smartthings.client;

import java.util.List;

import org.openhab.binding.smartthings.client.model.Endpoint;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SmartThingsAPI {

    @GET("smartapps/endpoints/{clientId}")
    Call<List<Endpoint>> getEndpoints(@Path("clientId") String clientId);

}
