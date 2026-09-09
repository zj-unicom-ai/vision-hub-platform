package com.unicom.visionhub.media.controller;


public record ConnectionHealth(String deviceId, boolean connected, boolean healthy, long lastFrameTime) {
}
