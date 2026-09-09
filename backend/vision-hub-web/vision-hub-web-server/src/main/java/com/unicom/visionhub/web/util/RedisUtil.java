package com.unicom.visionhub.web.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSemaphore;
import org.redisson.api.RSet;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedissonClient redissonClient;


    private <T> RBucket<T> getBucket(String key) {
        return redissonClient.getBucket(key);
    }


    public <T> void set(String key, T value) {
        getBucket(key).set(value);
    }


    public <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        getBucket(key).set(value, timeout, timeUnit);
    }


    public <T> void set(String key, T value, long seconds) {
        getBucket(key).set(value, seconds, TimeUnit.SECONDS);
    }


    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) getBucket(key).get();
    }


    public boolean delete(String key) {
        return getBucket(key).delete();
    }


    public long delete(Collection<String> keys) {
        return redissonClient.getKeys().delete(keys.toArray(new String[0]));
    }


    public boolean hasKey(String key) {
        return getBucket(key).isExists();
    }


    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return getBucket(key).expire(timeout, timeUnit);
    }


    public boolean expire(String key, long seconds) {
        return getBucket(key).expire(seconds, TimeUnit.SECONDS);
    }


    public long getExpire(String key) {
        long remainTimeMs = getBucket(key).remainTimeToLive();
        return remainTimeMs > 0 ? remainTimeMs / 1000 : -1;
    }


    public boolean clearExpire(String key) {
        return getBucket(key).clearExpire();
    }


    @SuppressWarnings("unchecked")
    public <V> V hGet(String key, String hashKey) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return (V) map.get(hashKey);
    }


    public <K, V> void hSet(String key, K hashKey, V value) {
        RMap<K, V> map = redissonClient.getMap(key);
        map.put(hashKey, value);
    }


    public <K, V> void hSet(String key, K hashKey, V value, long timeout, TimeUnit timeUnit) {
        RMap<K, V> map = redissonClient.getMap(key);
        map.put(hashKey, value);
        map.expire(timeout, timeUnit);
    }


    public <K, V> void hSetAll(String key, Map<K, V> map) {
        RMap<K, V> rMap = redissonClient.getMap(key);
        rMap.putAll(map);
    }


    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> hGetAll(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return (Map<K, V>) map.readAllMap();
    }


    public <K> long hDelete(String key, K... hashKeys) {
        RMap<K, Object> map = redissonClient.getMap(key);
        return map.fastRemove(hashKeys);
    }


    public <K> boolean hHasKey(String key, K hashKey) {
        RMap<K, Object> map = redissonClient.getMap(key);
        return map.containsKey(hashKey);
    }


    public long hSize(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.size();
    }


    @SuppressWarnings("unchecked")
    public <K> Set<K> hKeys(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return (Set<K>) map.readAllKeySet();
    }


    @SuppressWarnings("unchecked")
    public <V> Collection<V> hValues(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return (Collection<V>) map.readAllValues();
    }


    public long hIncrement(String key, String hashKey) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        Object result = map.addAndGet(hashKey, 1);
        return result != null ? ((Number) result).longValue() : 0;
    }


    public long hIncrement(String key, String hashKey, long delta) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        Object result = map.addAndGet(hashKey, delta);
        return result != null ? ((Number) result).longValue() : 0;
    }


    @SafeVarargs
    public final <V> boolean sAdd(String key, V... values) {
        RSet<V> set = redissonClient.getSet(key);
        return set.addAll(List.of(values));
    }


    @SuppressWarnings("unchecked")
    public <V> Set<V> sMembers(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return (Set<V>) set.readAll();
    }


    public <V> boolean sIsMember(String key, V value) {
        RSet<V> set = redissonClient.getSet(key);
        return set.contains(value);
    }


    public int sSize(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }


    public <V> boolean sRemove(String key, V value) {
        RSet<V> set = redissonClient.getSet(key);
        return set.remove(value);
    }


    public <V> void lLeftPush(String key, V value) {
        RList<V> list = redissonClient.getList(key);
        list.add(value);
    }


    public <V> void lRightPush(String key, V value) {
        RList<V> list = redissonClient.getList(key);
        list.add(value);
    }


    @SuppressWarnings("unchecked")
    public <V> List<V> lRange(String key, int start, int end) {
        RList<Object> list = redissonClient.getList(key);
        return (List<V>) list.range(start, end);
    }


    @SuppressWarnings("unchecked")
    public <V> List<V> lAll(String key) {
        RList<Object> list = redissonClient.getList(key);
        return (List<V>) list.readAll();
    }


    public int lSize(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.size();
    }


    @SuppressWarnings("unchecked")
    public <V> V lIndex(String key, int index) {
        RList<Object> list = redissonClient.getList(key);
        return (V) list.get(index);
    }


    @SuppressWarnings("unchecked")
    public <V> V lLeftPop(String key) {
        RList<Object> list = redissonClient.getList(key);
        return (V) list.remove(0);
    }


    @SuppressWarnings("unchecked")
    public <V> V lRightPop(String key) {
        RList<Object> list = redissonClient.getList(key);
        return (V) list.remove(list.size() - 1);
    }


    public <V> boolean lRemove(String key, V value) {
        RList<V> list = redissonClient.getList(key);
        return list.remove(value);
    }


    public <V> void lClear(String key) {
        RList<V> list = redissonClient.getList(key);
        list.clear();
    }


    public <V> boolean zAdd(String key, V value, double score) {
        RScoredSortedSet<V> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.add(score, value);
    }


    @SuppressWarnings("unchecked")
    public <V> Collection<V> zRange(String key, int start, int end) {
        RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
        return (Collection<V>) sortedSet.valueRange(start, end);
    }


    @SuppressWarnings("unchecked")
    public <V> Collection<V> zReverseRange(String key, int start, int end) {
        RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
        return (Collection<V>) sortedSet.valueRangeReversed(start, end);
    }


    @SuppressWarnings("unchecked")
    public <V> Collection<V> zRangeByScore(String key, double min, double max) {
        RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
        return (Collection<V>) sortedSet.valueRange(min, true, max, true);
    }


    public <V> Double zScore(String key, V value) {
        RScoredSortedSet<V> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.getScore(value);
    }


    public <V> Integer zRank(String key, V value) {
        RScoredSortedSet<V> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.rank(value);
    }


    public <V> Integer zReverseRank(String key, V value) {
        RScoredSortedSet<V> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.revRank(value);
    }


    public <V> boolean zRemove(String key, V value) {
        RScoredSortedSet<V> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.remove(value);
    }


    public <V> Double zIncrementScore(String key, V value, double delta) {
        RScoredSortedSet<V> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.addScore(value, delta);
    }


    public int zSize(String key) {
        RScoredSortedSet<Object> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.size();
    }


    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }


    public boolean tryLock(String lockKey) {
        RLock lock = getLock(lockKey);
        return lock.tryLock();
    }


    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        try {
            RLock lock = getLock(lockKey);
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            log.error("【Redis】获取锁失败: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }


    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }


    public boolean isLocked(String lockKey) {
        return getLock(lockKey).isLocked();
    }


    public RReadWriteLock getReadWriteLock(String lockKey) {
        return redissonClient.getReadWriteLock(lockKey);
    }


    public RSemaphore getSemaphore(String semaphoreKey) {
        return redissonClient.getSemaphore(semaphoreKey);
    }


    public boolean semaphoreAcquire(String semaphoreKey) {
        return getSemaphore(semaphoreKey).tryAcquire();
    }


    public void semaphoreRelease(String semaphoreKey) {
        getSemaphore(semaphoreKey).release();
    }


    public RCountDownLatch getCountDownLatch(String countDownLatchKey) {
        return redissonClient.getCountDownLatch(countDownLatchKey);
    }


    public <T> RBloomFilter<T> getBloomFilter(String bloomFilterKey) {
        return redissonClient.getBloomFilter(bloomFilterKey);
    }


    public <T> void initBloomFilter(String bloomFilterKey, long expectedInsertions, double falseProbability) {
        RBloomFilter<T> bloomFilter = getBloomFilter(bloomFilterKey);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
    }


    public <T> boolean bloomFilterAdd(String bloomFilterKey, T value) {
        return getBloomFilter(bloomFilterKey).add(value);
    }


    public <T> boolean bloomFilterContains(String bloomFilterKey, T value) {
        return getBloomFilter(bloomFilterKey).contains(value);
    }


    public RRateLimiter getRateLimiter(String rateLimiterKey) {
        return redissonClient.getRateLimiter(rateLimiterKey);
    }


    public void initRateLimiter(String rateLimiterKey, long rate, long rateInterval, TimeUnit timeUnit) {
        RRateLimiter rateLimiter = getRateLimiter(rateLimiterKey);
        RateIntervalUnit intervalUnit = switch (timeUnit) {
            case DAYS -> RateIntervalUnit.DAYS;
            case HOURS -> RateIntervalUnit.HOURS;
            case MINUTES -> RateIntervalUnit.MINUTES;
            case SECONDS -> RateIntervalUnit.SECONDS;
            case MILLISECONDS -> RateIntervalUnit.MILLISECONDS;
            default -> RateIntervalUnit.SECONDS;
        };
        rateLimiter.setRate(RateType.OVERALL, rate, rateInterval, intervalUnit);
    }


    public boolean rateLimiterTryAcquire(String rateLimiterKey) {
        return getRateLimiter(rateLimiterKey).tryAcquire();
    }


    public boolean rateLimiterTryAcquire(String rateLimiterKey, long permits, Duration duration) {
        return getRateLimiter(rateLimiterKey).tryAcquire(permits, duration);
    }


    public Iterable<String> keys(String pattern) {
        return redissonClient.getKeys().getKeysByPattern(pattern);
    }


    public long getDbSize() {
        return redissonClient.getKeys().count();
    }


    public void flushDb() {
        redissonClient.getKeys().flushdb();
    }
}
