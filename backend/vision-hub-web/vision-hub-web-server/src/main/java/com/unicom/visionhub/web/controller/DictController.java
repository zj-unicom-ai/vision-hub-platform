package com.unicom.visionhub.web.controller;

import com.unicom.visionhub.common.response.Result;
import com.unicom.visionhub.web.dto.request.DictQueryRequest;
import com.unicom.visionhub.web.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Tag(name = "字典查询", description = "统一字典数据查询接口")
public class DictController {

    private final DictService dictService;


    @PostMapping
    @Operation(summary = "查询字典", description = "根据类型查询字典数据，type=provider/model-type/model-version/algorithm-type，params 根据字典类型动态传递参数")
    public Result<List<Map<String, Object>>> queryDict(@Valid @RequestBody DictQueryRequest request) {
        log.info("【字典】查询字典: type={}", request.getType());
        List<Map<String, Object>> result = dictService.queryDict(request.getType(), request.getParams());
        return Result.success(result);
    }
}
