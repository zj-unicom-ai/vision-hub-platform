package com.unicom.visionhub.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询请求")
public class UserQueryRequest extends PageRequest {


    @Schema(description = "用户名（模糊查询）", example = "unicom")
    private String username;


    @Schema(description = "昵称（模糊查询）", example = "管理员")
    private String nickname;


    @Schema(description = "状态: 0-禁用, 1-启用", example = "1")
    private Integer status;
}