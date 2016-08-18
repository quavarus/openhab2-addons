package org.openhab.binding.smartthings.client.model;

public class Endpoint {
    private java.net.URL uri;
    private java.net.URI url;
    private java.net.URL base_url;
    private OAuthClient oauthClient;
    private Location location;

    public java.net.URL getUri() {
        return uri;
    }

    public java.net.URI getUrl() {
        return url;
    }

    public java.net.URL getBase_url() {
        return base_url;
    }

    public OAuthClient getOauthClient() {
        return oauthClient;
    }

    public Location getLocation() {
        return location;
    }

}
