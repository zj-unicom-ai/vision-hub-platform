package com.unicom.visionhub.web.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_upload_log")
@Schema(description = "文件上传日志实体")
public class FileUploadLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;


    @Schema(description = "业务类型")
    private String businessType;


    @Schema(description = "原始文件名")
    private String fileName;


    @Schema(description = "文件大小(字节)")
    private Long fileSize;


    @Schema(description = "文件类型(MIME类型)")
    private String fileType;


    @Schema(description = "文件扩展名")
    private String fileExtension;


    @Schema(description = "MinIO存储对象名(相对路径)")
    private String objectName;


    @Schema(description = "MinIO完整访问URL")
    private String minioUrl;


    @Schema(description = "MinIO桶名称")
    private String bucketName;


    @Schema(description = "上传状态: 0-失败, 1-成功")
    private Integer uploadStatus;


    @Schema(description = "错误信息")
    private String errorMessage;


    @Schema(description = "上传者IP地址")
    private String ipAddress;


    @Schema(description = "用户代理")
    private String userAgent;
}
