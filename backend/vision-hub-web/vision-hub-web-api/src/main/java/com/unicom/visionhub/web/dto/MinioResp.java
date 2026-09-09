package com.unicom.visionhub.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.io.Serial;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MinIO 文件上传响应")
public class MinioResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "原始文件名")
    private String fileName;


    @Schema(description = "文件大小（字节）")
    private Long fileSize;


    @Schema(description = "文件MD5")
    private String fileMd5;


    @Schema(description = "对象名称（包含路径）")
    private String objectName;


    @Schema(description = "存储桶名称")
    private String bucketName;


    @Schema(description = "文件访问URL")
    private String url;
}
