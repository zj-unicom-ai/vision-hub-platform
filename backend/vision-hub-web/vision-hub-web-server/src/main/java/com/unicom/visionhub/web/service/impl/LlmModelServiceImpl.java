package com.unicom.visionhub.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.visionhub.common.enums.ModelType;
import com.unicom.visionhub.common.enums.Provider;
import com.unicom.visionhub.common.enums.StatusCode;

import com.unicom.visionhub.common.exception.ResourceNotFoundException;
import com.unicom.visionhub.common.exception.ServiceException;
import com.unicom.visionhub.common.exception.SystemException;
import com.unicom.visionhub.web.cache.LlmModelCache;
import com.unicom.visionhub.web.dto.request.LlmModelCreateRequest;
import com.unicom.visionhub.web.dto.request.LlmModelQueryRequest;
import com.unicom.visionhub.web.dto.request.LlmModelUpdateRequest;
import com.unicom.visionhub.web.entity.model.LlmModel;
import com.unicom.visionhub.web.mapper.AlgorithmMapper;
import com.unicom.visionhub.web.mapper.LlmModelMapper;
import com.unicom.visionhub.web.service.LlmModelService;
import com.unicom.visionhub.web.service.MinioService;
import com.unicom.visionhub.web.vo.LlmModelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LlmModelServiceImpl extends ServiceImpl<LlmModelMapper, LlmModel> implements LlmModelService {

    private final LlmModelMapper llmModelMapper;
    private final AlgorithmMapper algorithmMapper;
    private final MinioService minioService;
    private final LlmModelCache llmModelCache;

    @Override
    public IPage<LlmModelVO> page(LlmModelQueryRequest queryRequest) {
        Page<LlmModel> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<LlmModelVO> resultPage = llmModelMapper.selectPageWithVO(page, queryRequest);

        resultPage.getRecords().forEach(this::enrichVO);

        return resultPage;
    }

    @Override
    public LlmModelVO getDetailById(Long id) {
        LlmModelVO vo = llmModelCache.getLlmModelVO(id);
        if (vo == null) {
            log.warn("【大模型】查询大模型失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.MODEL_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(LlmModelCreateRequest createRequest) {
        if (StrUtil.isBlank(createRequest.getModelType())
                || ModelType.fromCode(createRequest.getModelType()) == null) {
            log.warn("【大模型】创建大模型失败，模型类型无效: {}", createRequest.getModelType());
            throw new ServiceException("模型类型无效，仅支持: llm(大语言模型)");
        }

        if (StrUtil.isBlank(createRequest.getProvider())
                || Provider.fromCode(createRequest.getProvider()) == null) {
            log.warn("【大模型】创建大模型失败，供应商无效: {}", createRequest.getProvider());
            throw new ServiceException("供应商无效，仅支持: openai(OpenAI), qwen(通义千问), yuanjing(联通元景)");
        }

        LlmModel entity = BeanUtil.copyProperties(createRequest, LlmModel.class);
        entity.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);

        boolean saved = save(entity);
        if (!saved) {
            log.error("【大模型】创建大模型失败，数据库操作失败");
            throw new SystemException("创建大模型失败");
        }

        log.info("【大模型】创建大模型成功, id={}, name={}", entity.getId(), entity.getModelName());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(LlmModelUpdateRequest updateRequest) {
        LlmModel existEntity = getById(updateRequest.getId());
        if (existEntity == null) {
            log.warn("【大模型】更新大模型失败，id={} 不存在", updateRequest.getId());
            throw new ResourceNotFoundException(StatusCode.MODEL_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(updateRequest.getModelType())
                && ModelType.fromCode(updateRequest.getModelType()) == null) {
            log.warn("【大模型】更新大模型失败，模型类型无效: {}", updateRequest.getModelType());
            throw new ServiceException("模型类型无效，仅支持: llm(大语言模型)");
        }

        if (StrUtil.isNotBlank(updateRequest.getProvider())
                && Provider.fromCode(updateRequest.getProvider()) == null) {
            log.warn("【大模型】更新大模型失败，供应商无效: {}", updateRequest.getProvider());
            throw new ServiceException("供应商无效，仅支持: openai(OpenAI), qwen(通义千问), yuanjing(联通元景)");
        }

        LambdaUpdateWrapper<LlmModel> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LlmModel::getId, updateRequest.getId())
                .set(StrUtil.isNotBlank(updateRequest.getModelName()), LlmModel::getModelName, updateRequest.getModelName())
                .set(StrUtil.isNotBlank(updateRequest.getModelId()), LlmModel::getModelId, updateRequest.getModelId())
                .set(StrUtil.isNotBlank(updateRequest.getModelType()), LlmModel::getModelType, updateRequest.getModelType())
                .set(StrUtil.isNotBlank(updateRequest.getProvider()), LlmModel::getProvider, updateRequest.getProvider())
                .set(StrUtil.isNotBlank(updateRequest.getApiUrl()), LlmModel::getApiUrl, updateRequest.getApiUrl())
                .set(StrUtil.isNotBlank(updateRequest.getApiKey()), LlmModel::getApiKey, updateRequest.getApiKey())
                .set(StrUtil.isNotBlank(updateRequest.getModelVersion()), LlmModel::getModelVersion, updateRequest.getModelVersion())
                .set(StrUtil.isNotBlank(updateRequest.getIcon()), LlmModel::getIcon, updateRequest.getIcon())
                .set(StrUtil.isNotBlank(updateRequest.getDescription()), LlmModel::getDescription, updateRequest.getDescription())
                .set(StrUtil.isNotBlank(updateRequest.getRemark()), LlmModel::getRemark, updateRequest.getRemark())
                .set(updateRequest.getStatus() != null, LlmModel::getStatus, updateRequest.getStatus())
                .set(updateRequest.getSortOrder() != null, LlmModel::getSortOrder, updateRequest.getSortOrder())
                .set(LlmModel::getContextLength, updateRequest.getContextLength())
                .set(LlmModel::getMaxTokens, updateRequest.getMaxTokens());

        boolean updated = update(updateWrapper);
        if (!updated) {
            log.error("【大模型】更新大模型失败，数据库操作失败");
            throw new SystemException("更新大模型失败");
        }

        log.info("【大模型】更新大模型成功, id={}", updateRequest.getId());

        llmModelCache.evictLlmModel(updateRequest.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        LlmModel existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【大模型】删除大模型失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.MODEL_NOT_FOUND);
        }

        int algorithmCount = algorithmMapper.countByLlmModelId(id);
        if (algorithmCount > 0) {
            log.warn("【大模型】删除大模型失败，被 {} 个算法引用", algorithmCount);
            throw new ServiceException(StatusCode.MODEL_IN_USE_BY_ALGORITHM.getMessage());
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【大模型】删除大模型失败，数据库操作失败");
            throw new SystemException("删除大模型失败");
        }

        log.info("【大模型】删除大模型成功, id={}", id);

        llmModelCache.evictLlmModel(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        LlmModel existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【大模型】更新大模型状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.MODEL_NOT_FOUND);
        }

        if (status != null && status == 0) {
            int algorithmCount = algorithmMapper.countByLlmModelId(id);
            if (algorithmCount > 0) {
                log.warn("【大模型】停用大模型失败，被 {} 个算法引用", algorithmCount);
                throw new ServiceException(StatusCode.MODEL_DISABLED_IN_USE.getMessage());
            }
        }

        LlmModel entity = new LlmModel();
        entity.setId(id);
        entity.setStatus(status);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【大模型】更新大模型状态失败，数据库操作失败");
            throw new SystemException("更新状态失败");
        }

        log.info("【大模型】更新大模型状态成功, id={}, status={}", id, status);
        return true;
    }


    private void enrichVO(LlmModelVO vo) {
        ModelType modelType = ModelType.fromCode(vo.getModelType());
        vo.setModelTypeDesc(modelType != null ? modelType.getDescription() : "其他");

        Provider provider = Provider.fromCode(vo.getProvider());
        vo.setProviderDesc(provider != null ? provider.getDescription() : "其他");

        vo.setStatusDesc(vo.getStatus() == 1 ? "启用" : "禁用");

        if (StrUtil.isNotBlank(vo.getIcon())) {
            vo.setIconUrl(minioService.getFileUrlWithCdn(vo.getIcon()));
        }

        maskApiKey(vo);
    }


    private void maskApiKey(LlmModelVO vo) {
        if (StrUtil.isNotBlank(vo.getApiKey())) {
            String apiKey = vo.getApiKey();
            if (apiKey.length() > 10) {
                vo.setApiKey(apiKey.substring(0, 8) + "****" + apiKey.substring(apiKey.length() - 4));
            } else {
                vo.setApiKey("****");
            }
        }
    }
}
