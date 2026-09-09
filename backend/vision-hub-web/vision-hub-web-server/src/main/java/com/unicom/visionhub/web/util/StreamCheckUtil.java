package com.unicom.visionhub.web.util;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;


@Slf4j
public class StreamCheckUtil {


    public static StreamCheckResult checkStreamUrl(String streamUrl, int timeoutSeconds) {
        if (StrUtil.isBlank(streamUrl)) {
            return new StreamCheckResult(false, "视频流地址为空");
        }

        FFmpegFrameGrabber grabber = null;

        try {
            grabber = new FFmpegFrameGrabber(streamUrl);
            grabber.setOption("stimeout", String.valueOf(timeoutSeconds * 1000000));
            grabber.setOption("fflags", "nobuffer");
            grabber.setOption("flags", "low_delay");
            grabber.setOption("probesize", "500000");
            grabber.setOption("analyzeduration", "1000000");

            long startTime = System.currentTimeMillis();
            grabber.start();
            long connectTime = System.currentTimeMillis() - startTime;

            log.info("【视频流检测】检测成功: {}, 耗时: {}ms", streamUrl, connectTime);
            return new StreamCheckResult(true, "检测成功，耗时" + connectTime + "ms");

        } catch (Exception e) {
            log.warn("【视频流检测】检测失败: {}, 原因: {}", streamUrl, e.getMessage());
            return new StreamCheckResult(false, "连接失败: " + e.getMessage());
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    log.debug("【视频流检测】释放资源异常: {}", e.getMessage());
                }
            }
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamCheckResult {

        private boolean success;


        private String message;
    }
}
