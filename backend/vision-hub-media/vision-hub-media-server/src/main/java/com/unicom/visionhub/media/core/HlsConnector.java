package com.unicom.visionhub.media.core;

import com.unicom.visionhub.media.config.MediaProperties;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.util.concurrent.ExecutorService;


public class HlsConnector extends AbstractFfmpegConnector {

    public HlsConnector(String deviceId, String streamUrl, FrameBuffer frameBuffer,
                        MediaProperties properties, ExecutorService grabberExecutor) {
        super(deviceId, streamUrl, frameBuffer, properties, grabberExecutor);
    }

    @Override
    public String getProtocol() {
        return "hls";
    }

    @Override
    protected void configureGrabber(FFmpegFrameGrabber grabber) {
        grabber.setOption("timeout", String.valueOf(properties.getReadTimeout() * 1000));
        grabber.setOption("http_multiple", "1");
        grabber.setOption("fflags", "nobuffer");
        grabber.setOption("flags", "low_delay");
        grabber.setOption("readahead", "0");
    }
}
