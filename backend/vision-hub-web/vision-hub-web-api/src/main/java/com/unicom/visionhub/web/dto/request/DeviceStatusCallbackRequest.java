package com.unicom.visionhub.web.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class DeviceStatusCallbackRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private String deviceStatus;


    private String checkStatus;
}
