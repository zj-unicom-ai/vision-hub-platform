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
import com.unicom.visionhub.web.dto.request.IndustryCreateRequest;
import com.unicom.visionhub.web.dto.request.IndustryListRequest;
import com.unicom.visionhub.web.dto.request.IndustryQueryRequest;
import com.unicom.visionhub.web.dto.request.IndustryUpdateRequest;
import com.unicom.visionhub.web.entity.industry.Industry;
import com.unicom.visionhub.web.mapper.IndustryMapper;
import com.unicom.visionhub.web.service.IndustryService;
import com.unicom.visionhub.web.vo.IndustryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryServiceImpl extends ServiceImpl<IndustryMapper, Industry> implements IndustryService {

    private final IndustryMapper industryMapper;

    @Override
    public IPage<IndustryVO> page(IndustryQueryRequest queryRequest) {
        Page<Industry> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        IPage<IndustryVO> resultPage = industryMapper.selectPageWithVO(page, queryRequest);
        resultPage.getRecords().forEach(this::enrichVO);
        return resultPage;
    }

    @Override
    public List<IndustryVO> list(IndustryListRequest listRequest) {
        List<IndustryVO> list = industryMapper.selectListWithVO(listRequest);
        list.forEach(this::enrichVO);
        return list;
    }

    @Override
    public IndustryVO getDetailById(Long id) {
        IndustryVO vo = industryMapper.selectVOById(id);
        if (vo == null) {
            log.warn("【行业】查询行业失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.INDUSTRY_NOT_FOUND);
        }
        enrichVO(vo);
        return vo;
    }


    private void enrichVO(IndustryVO vo) {
        vo.setStatusDesc(vo.getStatus() != null && vo.getStatus() == 1 ? "启用" : "禁用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(IndustryCreateRequest createRequest) {
        if (industryMapper.countByIndustryCode(createRequest.getIndustryCode(), null) > 0) {
            log.warn("【行业】创建行业失败，行业编码已存在: {}", createRequest.getIndustryCode());
            throw new ResourceExistsException("行业", "industry_code", createRequest.getIndustryCode());
        }

        Industry entity = BeanUtil.copyProperties(createRequest, Industry.class);
        entity.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);

        boolean saved = save(entity);
        if (!saved) {
            log.error("【行业】创建行业失败，数据库操作失败");
            throw new SystemException("创建行业失败");
        }

        log.info("【行业】创建行业成功, id={}, code={}", entity.getId(), entity.getIndustryCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(IndustryUpdateRequest updateRequest) {
        Industry existEntity = getById(updateRequest.getId());
        if (existEntity == null) {
            log.warn("【行业】更新行业失败，id={} 不存在", updateRequest.getId());
            throw new ResourceNotFoundException(StatusCode.INDUSTRY_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(updateRequest.getIndustryCode()) &&
                industryMapper.countByIndustryCode(updateRequest.getIndustryCode(), updateRequest.getId()) > 0) {
            log.warn("【行业】更新行业失败，行业编码已被占用: {}", updateRequest.getIndustryCode());
            throw new ResourceExistsException("行业", "industry_code", updateRequest.getIndustryCode());
        }

        Industry entity = BeanUtil.copyProperties(updateRequest, Industry.class);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【行业】更新行业失败，数据库操作失败");
            throw new SystemException("更新行业失败");
        }

        log.info("【行业】更新行业成功, id={}", updateRequest.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Industry existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【行业】删除行业失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.INDUSTRY_NOT_FOUND);
        }

        boolean removed = removeById(id);
        if (!removed) {
            log.error("【行业】删除行业失败，数据库操作失败");
            throw new SystemException("删除行业失败");
        }

        log.info("【行业】删除行业成功, id={}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        Industry existEntity = getById(id);
        if (existEntity == null) {
            log.warn("【行业】更新行业状态失败，id={} 不存在", id);
            throw new ResourceNotFoundException(StatusCode.INDUSTRY_NOT_FOUND);
        }

        Industry entity = new Industry();
        entity.setId(id);
        entity.setStatus(status);

        boolean updated = updateById(entity);
        if (!updated) {
            log.error("【行业】更新行业状态失败，数据库操作失败");
            throw new SystemException("更新状态失败");
        }

        log.info("【行业】更新行业状态成功, id={}, status={}", id, status);
        return true;
    }
}
