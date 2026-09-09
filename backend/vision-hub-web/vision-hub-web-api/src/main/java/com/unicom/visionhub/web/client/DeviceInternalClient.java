package com.unicom.visionhub.web.client;

import com.unicom.visionhub.web.dto.DeviceStatusCallbackDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "vision-hub-web",
        url = "${vision-hub.web.base-url}"
)
public interface DeviceInternalClient {


    @PostMapping("/api/internal/devices/{deviceId}/status-callback")
    void statusCallback(@PathVariable("deviceId") String deviceId,
                        @RequestBody DeviceStatusCallbackDto request);
}