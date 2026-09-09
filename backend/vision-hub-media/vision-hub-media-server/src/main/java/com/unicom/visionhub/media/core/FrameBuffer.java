package com.unicom.visionhub.media.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class FrameBuffer {

    private final ConcurrentHashMap<String, FrameEntry> buffer = new ConcurrentHashMap<>();


    public void update(String deviceId, BufferedImage frame) {
        buffer.put(deviceId, new FrameEntry(frame, System.currentTimeMillis()));
    }


    public BufferedImage getLatest(String deviceId) {
        FrameEntry entry = buffer.get(deviceId);
        if (entry == null) {
            return null;
        }
        return entry.frame();
    }


    public FrameEntry getLatestEntry(String deviceId) {
        return buffer.get(deviceId);
    }


    public void remove(String deviceId) {
        buffer.remove(deviceId);
        log.debug("【帧缓存】移除设备缓存: {}", deviceId);
    }


    public int size() {
        return buffer.size();
    }


    public record FrameEntry(BufferedImage frame, long timestamp) {
    }
}
