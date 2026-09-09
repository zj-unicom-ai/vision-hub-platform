package com.unicom.visionhub.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.io.Serial;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件元数据")
public class FileMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "对象名称")
    private String objectName;


    @Schema(description = "文件大小（字节）")
    private Long size;


    @Schema(description = "内容类型")
    private String contentType;


    @Schema(description = "最后修改时间")
    private LocalDateTime lastModified;

    /**
     * ETag
     */
    @Schema(description = "ETag")
    private String etag;


    @Schema(description = "用户自定义标签")
    private Map<String, String> tags;


    @Schema(description = "文件版本ID")
    private String versionId;
}
