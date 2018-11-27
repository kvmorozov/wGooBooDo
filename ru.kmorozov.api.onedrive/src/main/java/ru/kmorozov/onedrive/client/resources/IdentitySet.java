package ru.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;

public class IdentitySet {

    @Key
    private Identity user;
    @Key
    private Identity application;
    @Key
    private Identity device;

    public Identity getUser() {
        return this.user;
    }

    public Identity getApplication() {
        return this.application;
    }

    public Identity getDevice() {
        return this.device;
    }
}
