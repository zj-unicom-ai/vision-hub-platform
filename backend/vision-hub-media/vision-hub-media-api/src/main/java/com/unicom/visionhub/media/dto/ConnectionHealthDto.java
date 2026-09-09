package com.unicom.visionhub.media.dto;

import java.io.Serializable;


public class ConnectionHealthDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceId;
    private boolean connected;
    private boolean healthy;
    private long lastFrameTime;

    public ConnectionHealthDto() {
    }

    public ConnectionHealthDto(String deviceId, boolean connected, boolean healthy, long lastFrameTime) {
        this.deviceId = deviceId;
        this.connected = connected;
        this.healthy = healthy;
        this.lastFrameTime = lastFrameTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }
}