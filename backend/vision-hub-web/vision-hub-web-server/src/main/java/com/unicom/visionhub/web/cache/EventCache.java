package com.unicom.visionhub.web.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.visionhub.web.entity.event.Event;
import com.unicom.visionhub.web.mapper.EventMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventCache {

    private final EventMapper eventMapper;


    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, List<Event>>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        reloadAll();
    }


    public void reloadAll() {
        cache.clear();
        List<Event> allEvents = eventMapper.selectList(
                new LambdaQueryWrapper<Event>()
                        .eq(Event::getDeleted, 0));

        for (Event event : allEvents) {
            cache.computeIfAbsent(event.getTaskId(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(event.getAlgorithmId(), k -> Collections.synchronizedList(new java.util.ArrayList<>()))
                    .add(event);
        }

        int taskCount = cache.size();
        int totalEvents = allEvents.size();
        log.info("【EventCache】全量加载完成，共 {} 个任务，{} 个事件", taskCount, totalEvents);
    }


    public List<Event> getEvents(Long taskId, Long algorithmId) {
        Map<Long, List<Event>> taskEvents = cache.get(taskId);
        if (taskEvents == null) {
            return Collections.emptyList();
        }
        List<Event> events = taskEvents.get(algorithmId);
        return events != null ? Collections.unmodifiableList(events) : Collections.emptyList();
    }


    public void refreshTask(Long taskId) {
        List<Event> events = eventMapper.selectList(
                new LambdaQueryWrapper<Event>()
                        .eq(Event::getTaskId, taskId)
                        .eq(Event::getDeleted, 0));

        ConcurrentHashMap<Long, List<Event>> algoMap = new ConcurrentHashMap<>();
        Map<Long, List<Event>> grouped = events.stream()
                .collect(Collectors.groupingBy(Event::getAlgorithmId));
        grouped.forEach((algoId, list) -> algoMap.put(algoId, Collections.synchronizedList(new java.util.ArrayList<>(list))));

        cache.put(taskId, algoMap);
        log.debug("【EventCache】刷新任务缓存: taskId={}, events={}", taskId, events.size());
    }


    public void removeTask(Long taskId) {
        cache.remove(taskId);
        log.debug("【EventCache】移除任务缓存: taskId={}", taskId);
    }


    public void addEvent(Event event) {
        if (event == null || event.getDeleted() == 1) {
            return;
        }
        cache.computeIfAbsent(event.getTaskId(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(event.getAlgorithmId(), k -> Collections.synchronizedList(new java.util.ArrayList<>()))
                .add(event);
        log.debug("【EventCache】添加事件缓存: taskId={}, algorithmId={}, eventId={}",
                event.getTaskId(), event.getAlgorithmId(), event.getId());
    }
}
