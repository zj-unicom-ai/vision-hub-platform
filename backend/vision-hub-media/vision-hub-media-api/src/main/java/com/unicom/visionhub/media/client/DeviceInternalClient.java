package com.unicom.visionhub.media.client;

import com.unicom.visionhub.media.dto.ConnectionHealthDto;
import com.unicom.visionhub.media.dto.StreamStatsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(
        name = "vision-hub-media",
        url = "${vision-hub.media.base-url}",
        path = "/media"
)
public interface DeviceInternalClient {


    @PostMapping("/stream/{deviceId}")
    Boolean startStream(@PathVariable("deviceId") String deviceId,
                        @RequestParam("streamUrl") String streamUrl);


    @DeleteMapping("/stream/{deviceId}")
    void stopStream(@PathVariable("deviceId") String deviceId);


    @GetMapping("/stream/active")
    List<String> getActiveDevices();


    @GetMapping("/frame/{deviceId}")
    ResponseEntity<byte[]> captureFrame(@PathVariable("deviceId") String deviceId,
                                       @RequestParam(value = "format", defaultValue = "jpg") String format);


    @GetMapping("/health")
    List<ConnectionHealthDto> getConnectionHealth();


    @GetMapping("/stats")
    StreamStatsDto getStats();
}