package com.unicom.visionhub.media.core;

import com.unicom.visionhub.media.config.MediaProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegLogCallback;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public abstract class AbstractFfmpegConnector implements StreamConnector {


    static {
        avutil.av_log_set_level(avutil.AV_LOG_QUIET);
    }

    protected final String streamUrl;
    protected final String deviceId;
    protected final FrameBuffer frameBuffer;
    protected final MediaProperties properties;
    protected final ExecutorService grabberExecutor;

    protected FFmpegFrameGrabber grabber;
    protected Java2DFrameConverter converter;
    protected Future<?> grabTaskFuture;

    protected final AtomicBoolean running = new AtomicBoolean(false);
    protected final AtomicBoolean connected = new AtomicBoolean(false);

    @Getter
    protected volatile long lastFrameTime;

    protected AbstractFfmpegConnector(String deviceId, String streamUrl, FrameBuffer frameBuffer,
                                      MediaProperties properties, ExecutorService grabberExecutor) {
        this.deviceId = deviceId;
        this.streamUrl = streamUrl;
        this.frameBuffer = frameBuffer;
        this.properties = properties;
        this.grabberExecutor = grabberExecutor;
    }

    @Override
    public void start(String streamUrl) throws Exception {
        if (running.compareAndSet(false, true)) {
            try {
                doConnect();
                grabTaskFuture = grabberExecutor.submit(this::grabLoop);
                log.info("【媒体】{}拉流启动: deviceId={}, url={}", getProtocol().toUpperCase(), deviceId, streamUrl);
            } catch (Exception e) {
                running.set(false);
                log.error("【媒体】{}启动失败: deviceId={}, error={}", getProtocol().toUpperCase(), deviceId, e.getMessage());
                throw e;
            }
        }
    }

    private void doConnect() throws Exception {
        try {
            grabber = new FFmpegFrameGrabber(streamUrl);
            configureGrabber(grabber);

            grabber.start();

            converter = new Java2DFrameConverter();
            connected.set(true);
            log.debug("【媒体】{}连接建立: deviceId={}, url={}", getProtocol().toUpperCase(), deviceId, streamUrl);
        } catch (Exception e) {
            connected.set(false);
            cleanupGrabber();
            throw e;
        }
    }


    protected abstract void configureGrabber(FFmpegFrameGrabber grabber);


    private void grabLoop() {
        int retryCount = 0;

        while (running.get()) {
            try {
                if (!connected.get()) {
                    boolean reconnected = tryReconnect(retryCount);
                    if (reconnected) {
                        retryCount = 0;
                        if (!waitForFirstFrame(properties.getReadTimeout())) {
                            log.warn("【媒体】{}首帧超时，标记断线: deviceId={}",
                                    getProtocol().toUpperCase(), deviceId);
                            connected.set(false);
                            cleanupGrabber();
                        }
                    } else {
                        int maxRetries = properties.getMaxRetryAttempts();
                        if (maxRetries >= 0 && retryCount >= maxRetries) {
                            log.warn("【媒体】{}重试次数耗尽，停止拉流: deviceId={}, retries={}",
                                    getProtocol().toUpperCase(), deviceId, retryCount);
                            running.set(false);
                            break;
                        }
                        long retryInterval = properties.getRetryBaseInterval()
                                * (1L + (long) retryCount * properties.getRetryIntervalMultiplier());
                        sleepQuietly(retryInterval);
                        retryCount++;
                    }
                    continue;
                }

                Frame frame = grabber.grabImage();
                if (frame != null && frame.image != null) {
                    BufferedImage image = converter.getBufferedImage(frame);
                    if (image != null) {
                        frameBuffer.update(deviceId, image);
                        lastFrameTime = System.currentTimeMillis();
                    }
                } else {
                    if (connected.get() && System.currentTimeMillis() - lastFrameTime > properties.getFrameExpireTime()) {
                        log.warn("【媒体】{}帧超时，标记断线等待重连: deviceId={}, lastFrameTime={}ms ago",
                                getProtocol().toUpperCase(), deviceId, System.currentTimeMillis() - lastFrameTime);
                        connected.set(false);
                        cleanupGrabber();
                    }
                }
            } catch (Exception e) {
                log.warn("【媒体】{}拉流异常，标记断线: deviceId={}, error={}",
                        getProtocol().toUpperCase(), deviceId, e.getMessage());
                connected.set(false);
                cleanupGrabber();
                sleepQuietly(properties.getRetryBaseInterval());
            }
        }
    }


    private boolean waitForFirstFrame(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (running.get() && connected.get()) {
            try {
                Frame frame = grabber.grabImage();
                if (frame != null && frame.image != null) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("【媒体】等待首帧异常: deviceId={}, error={}", deviceId, e.getMessage());
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                log.debug("【媒体】等待首帧超时: deviceId={}, timeout={}ms", deviceId, timeoutMs);
                return false;
            }
            sleepQuietly(100);
        }
        return false;
    }


    private boolean tryReconnect(int retryCount) {
        try {
            log.info("【媒体】{}尝试重连: deviceId={}, url={}, retry={}",
                    getProtocol().toUpperCase(), deviceId, streamUrl, retryCount);
            doConnect();
            return true;
        } catch (Exception e) {
            log.warn("【媒体】{}重连失败: deviceId={}, error={}",
                    getProtocol().toUpperCase(), deviceId, e.getMessage());
            return false;
        }
    }

    @Override
    public BufferedImage grabFrame() {
        if (!connected.get()) {
            return null;
        }
        var entry = frameBuffer.getLatestEntry(deviceId);
        if (entry != null) {
            return entry.frame();
        }
        return null;
    }

    @Override
    public boolean isHealthy() {
        if (!running.get() || !connected.get()) {
            return false;
        }
        long idleTime = System.currentTimeMillis() - lastFrameTime;
        return idleTime < properties.getFrameExpireTime();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (grabTaskFuture != null) {
                grabTaskFuture.cancel(true);
            }
            cleanupGrabber();
            frameBuffer.remove(deviceId);
            log.info("【媒体】{}拉流停止: deviceId={}, url={}", getProtocol().toUpperCase(), deviceId, streamUrl);
        }
    }

    private void cleanupGrabber() {
        if (converter != null) {
            try {
                converter.close();
            } catch (Exception ignored) {
            }
            converter = null;
        }
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception ignored) {
            }
            grabber = null;
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getStreamUrl() {
        return streamUrl;
    }
}