package com.unicom.visionhub.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.visionhub.common.enums.StatusCode;
import com.unicom.visionhub.common.exception.ResourceExistsException;
import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.dto.OutputFormatField;
import com.unicom.visionhub.web.dto.request.PromptTemplateCreateRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateListRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateQueryRequest;
import com.unicom.visionhub.web.dto.request.PromptTemplateUpdateRequest;
import com.unicom.visionhub.web.entity.prompt.PromptTemplate;
import com.unicom.visionhub.web.entity.prompt.PromptTemplateIndustry;
import com.unicom.visionhub.web.mapper.PromptTemplateIndustryMapper;
import com.unicom.visionhub.web.mapper.PromptTemplateMapper;
import com.unicom.visionhub.web.service.PromptTemplateService;
import com.unicom.visionhub.web.vo.PromptTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl extends ServiceImpl<PromptTemplateMapper, PromptTemplate>
        implements PromptTemplateService {

    private final PromptTemplateMapper promptTemplateMapper;
    private final PromptTemplateIndustryMapper promptTemplateIndustryMapper;

    @Override
    public IPage<PromptTemplateVO> page(PromptTemplateQueryRequest queryRequest) {
        Page<PromptTemplate> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<PromptTemplateVO> resultPage = promptTemplateMapper.selectPageWithVO(page, queryRequest);
        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }

    @Override
    public PromptTemplateVO getDetailById(Long id) {
        PromptTemplateVO vo = promptTemplateMapper.selectVOById(id);
        if (vo == null) {
            log.warn("【提示词模板】查询提示词模板失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.PROMPT_NOT_FOUND);
        }
        enrichVO(vo);
        return vo;
    }


    private void enrichVO(PromptTemplateVO vo) {
        vo.setStatusDesc(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PromptTemplateCreateRequest createRequest) {
        if (promptTemplateMapper.countByTemplateName(createRequest.getTemplateName(), null) > 0) {
            log.warn("【提示词模板】创建提示词模板失败，模板名称已存在: {}", createRequest.getTemplateName());
            throw new ResourceExistsException("提示词模板", "template_name", createRequest.getTemplateName());
        }

        PromptTemplate entity = BeanUtil.copyProperties(createRequest, PromptTemplate.class);
        entity.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);

        boolean saved = save(entity);
        if (!saved) {
            log.error("【提示词模板】创建提示词模板失败，数据库操作失败");
            throw new SystemException("创建提示词模板失败");
        }

        List<Long> industryIds = createRequest.getIndustryIds();
        if (industryIds != null && !industryIds.isEmpty()) {
            for (Long industryId : industryIds) {
                PromptTemplateIndustry relation = new PromptTemplateIndustry();
                relation.setTemplateId(entity.getId());
                relation.setIndustryId(industryId);
                relation.setCreatedBy(entity.getCreatedBy());
                promptTemplateIndustryMapper.insert(relation);
            }
        }

        log.info("【提示词模板】创建提示词模板成功, id={}, name={}", entity.getId(), entity.getTemplateName());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PromptTemplateUpdateRequest updateRequest) {
        PromptTemplate existEntity = getById(updateRequest.getId());
        if (existEntity == null) {
            log.warn("【提示词模板】更新提示词模板失败，id={} 不存在", updateRequest.getId());
            throw new ResourceNotFoundException(StatusCode.PROMPT_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(updateRequest.getTemplateName()) &&
                promptTemplateMapper.countByTemplateName(updateRequest.getTemplateName(), updateRequest.getId()) > 0) {
            log.warn("【提示词模板】更新提示词模板失败，模板名称已被占用: {}", updateRequest.getTemplateName());
            throw new ResourceExistsException("提示词模板", "template_name", updateRequest.getTemplateName());
        }

        PromptTemplate entity = BeanUtil.copyProperties(updateRequest, PromptTemplate.class);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【提示词模板】更新提示词模板失败，数据库操作失败");
            throw new SystemException("更新提示词模板失败");
        }

        List<Long> industryIds = updateRequest.getIndustryIds();
        if (industryIds != null) {
            promptTemplateIndustryMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromptTemplateIndustry>()
                            .eq(PromptTemplateIndustry::getTemplateId, updateRequest.getId())
            );

            for (Long industryId : industryIds) {
                PromptTemplateIndustry relation = new PromptTemplateIndustry();
                relation.setTemplateId(updateRequest.getId());
                relation.setIndustryId(industryId);
                relation.setCreatedBy(entity.getUpdatedBy());
                promptTemplateIndustryMapper.insert(relation);
            }
        }

        log.info("【提示词模板】更新提示词模板成功, id={}", updateRequest.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        PromptTemplate existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【提示词模板】删除提示词模板失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.PROMPT_NOT_FOUND);
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【提示词模板】删除提示词模板失败，数据库操作失败");
            throw new SystemException("删除提示词模板失败");
        }

        log.info("【提示词模板】删除提示词模板成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        PromptTemplate existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【提示词模板】更新提示词模板状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.PROMPT_NOT_FOUND);
        }

        PromptTemplate entity = new PromptTemplate();
        entity.setId(id);
        entity.setStatus(status);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【提示词模板】更新提示词模板状态失败，数据库操作失败");
            throw new SystemException("更新状态失败");
        }

        log.info("【提示词模板】更新提示词模板状态成功, id={}, status={}", id, status);
        return true;
    }

    @Override
    public String getFullPromptById(Long id) {
        PromptTemplate entity = getById(id);
        if (entity == null) {
            log.warn("【提示词模板】获取完整提示词失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.PROMPT_NOT_FOUND);
        }

        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(entity.getTemplateContent());

        if (entity.getOutputFormat() != null && !entity.getOutputFormat().isEmpty()) {
            fullPrompt.append("\n\n输出格式：\n");
            String formatText = buildOutputFormatText(entity.getOutputFormat());
            fullPrompt.append(formatText);
        }

        return fullPrompt.toString();
    }

    private String buildOutputFormatText(Map<String, OutputFormatField> outputFormat) {
        return outputFormat.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    OutputFormatField field = entry.getValue();
                    StringBuilder sb = new StringBuilder();
                    sb.append(key).append(": ");
                    sb.append(field.getName());
                    if (StrUtil.isNotBlank(field.getDesc())) {
                        sb.append("（").append(field.getDesc()).append("）");
                    }
                    if (Boolean.TRUE.equals(field.getRequired())) {
                        sb.append(" [必填]");
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    @Override
    public List<PromptTemplateVO> list(PromptTemplateListRequest listRequest) {
        List<PromptTemplateVO> list = promptTemplateMapper.selectListByCondition(listRequest);
        list.forEach(this::enrichVO);
        return list;
    }
}
