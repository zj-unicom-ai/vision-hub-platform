package com.unicom.visionhub.web.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class AlgorithmResultMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Long taskId;


    private Long deviceId;


    private Long algorithmId;


    private String resultImage;


    private Long executeDuration;


    private String analysisResult;


    private BigDecimal confidence;
}
