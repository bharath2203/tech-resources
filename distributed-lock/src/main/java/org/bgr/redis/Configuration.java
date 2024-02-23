package org.bgr.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;

public class Configuration {

    public RedissonClient redissonClient() {
        return Redisson.create();
    }

}
