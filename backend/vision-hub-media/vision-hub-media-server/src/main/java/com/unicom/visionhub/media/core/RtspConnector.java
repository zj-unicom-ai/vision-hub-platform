package com.unicom.visionhub.media.core;

import com.unicom.visionhub.media.config.MediaProperties;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.util.concurrent.ExecutorService;


public class RtspConnector extends AbstractFfmpegConnector {

    public RtspConnector(String deviceId, String streamUrl, FrameBuffer frameBuffer,
                         MediaProperties properties, ExecutorService grabberExecutor) {
        super(deviceId, streamUrl, frameBuffer, properties, grabberExecutor);
    }

    @Override
    public String getProtocol() {
        return "rtsp";
    }

    @Override
    protected void configureGrabber(FFmpegFrameGrabber grabber) {
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setOption("timeout", String.valueOf(properties.getReadTimeout() * 1000));
        grabber.setOption("stimeout", String.valueOf(properties.getReadTimeout() * 1000));
        grabber.setOption("fflags", "nobuffer");
        grabber.setOption("flags", "low_delay");
        grabber.setOption("probesize", "500000");
        grabber.setOption("analyzeduration", "500000");
    }
}
