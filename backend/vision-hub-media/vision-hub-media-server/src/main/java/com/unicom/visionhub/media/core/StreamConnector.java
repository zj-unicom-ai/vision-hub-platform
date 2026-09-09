package com.unicom.visionhub.media.core;

import java.awt.image.BufferedImage;


public interface StreamConnector {


    String getProtocol();


    void start(String streamUrl) throws Exception;


    BufferedImage grabFrame();


    boolean isHealthy();


    boolean isRunning();


    long getLastFrameTime();


    void stop();


    String getStreamUrl();
}
