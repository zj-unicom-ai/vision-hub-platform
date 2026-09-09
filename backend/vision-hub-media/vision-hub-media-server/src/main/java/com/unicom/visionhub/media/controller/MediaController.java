package com.unicom.visionhub.media.controller;

import com.unicom.visionhub.media.service.MediaService;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;


    @PostMapping("/stream/{deviceId}")
    public boolean startStream(
            @PathVariable String deviceId,
            @RequestParam String streamUrl) {
        log.info("【媒体】收到启动流请求: deviceId={}, url={}", deviceId, streamUrl);
        return mediaService.startStream(deviceId, streamUrl);
    }


    @DeleteMapping("/stream/{deviceId}")
    public void stopStream(
            @PathVariable String deviceId) {
        log.info("【媒体】收到停止流请求: deviceId={}", deviceId);
        mediaService.stopStream(deviceId);
    }


    @GetMapping("/frame/{deviceId}")
    public void getFrame(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "jpg") String format,
            HttpServletResponse response) throws IOException {
        byte[] imageBytes = mediaService.captureFrame(deviceId, format);

        if (imageBytes == null || imageBytes.length == 0) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("设备未接入或帧缓存为空");
            return;
        }

        String contentType = "jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)
                ? MediaType.IMAGE_JPEG_VALUE
                : MediaType.IMAGE_PNG_VALUE;

        response.setContentType(contentType);
        response.setContentLength(imageBytes.length);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        try (OutputStream out = response.getOutputStream()) {
            out.write(imageBytes);
            out.flush();
        }
    }


    @GetMapping("/stats")
    public StreamStats getStats() {
        return new StreamStats(mediaService.getActiveStreamCount());
    }


    @GetMapping("/stream/active")
    public List<String> getActiveDevices() {
        return mediaService.getActiveDeviceIds();
    }


    @GetMapping("/health")
    public List<ConnectionHealth> getConnectionHealth() {
        return mediaService.getConnectionHealth();
    }

    public record StreamStats(int activeStreamCount) {
    }
}
