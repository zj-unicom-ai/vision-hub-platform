package com.unicom.visionhub.web.service;


public interface FrameCaptureService {


    @Deprecated
    String capture(String streamUrl);


    String captureByDevice(Long deviceId, String streamUrl);
}
