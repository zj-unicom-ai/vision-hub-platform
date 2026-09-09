package com.unicom.visionhub.media.dto;

import java.io.Serializable;


public class StreamStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int activeStreamCount;

    public StreamStatsDto() {
    }

    public StreamStatsDto(int activeStreamCount) {
        this.activeStreamCount = activeStreamCount;
    }

    public int getActiveStreamCount() {
        return activeStreamCount;
    }

    public void setActiveStreamCount(int activeStreamCount) {
        this.activeStreamCount = activeStreamCount;
    }
}