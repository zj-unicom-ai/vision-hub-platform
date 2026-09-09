package com.unicom.visionhub.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class DeviceStatusCallbackDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String deviceStatus;

    private String checkStatus;
}