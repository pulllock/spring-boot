/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.cache;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * Redis cache configuration.
 *
 * redis缓存配置
 *
 * @author Stephane Nicoll
 * @author Mark Paluch
 * @author Ryon Day
 */
@Configuration(proxyBeanMethods = false)
// 需要RedisConnectionFactory类存在
@ConditionalOnClass(RedisConnectionFactory.class)
// 在RedisAutoConfiguration自动配置之后进行配置
@AutoConfigureAfter(RedisAutoConfiguration.class)
// 需要RedisConnectionFactory的Bean存在
@ConditionalOnBean(RedisConnectionFactory.class)
// 不能存在CacheManager的Bean
@ConditionalOnMissingBean(CacheManager.class)
// 需要满足CacheCondition的条件
@Conditional(CacheCondition.class)
class RedisCacheConfiguration {

	@Bean
	RedisCacheManager cacheManager(CacheProperties cacheProperties, CacheManagerCustomizers cacheManagerCustomizers,
			ObjectProvider<org.springframework.data.redis.cache.RedisCacheConfiguration> redisCacheConfiguration,
			ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers,
			RedisConnectionFactory redisConnectionFactory, ResourceLoader resourceLoader) {
		// 创建RedisCacheManagerBuilder实例
		RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(
				determineConfiguration(cacheProperties, redisCacheConfiguration, resourceLoader.getClassLoader()));
		List<String> cacheNames = cacheProperties.getCacheNames();
		if (!cacheNames.isEmpty()) {
			// 初始化配置中配置的缓存
			builder.initialCacheNames(new LinkedHashSet<>(cacheNames));
		}
		// 先使用RedisCacheManagerBuilder进行自定义
		redisCacheManagerBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		// 创建RedisCacheManager，并使用CacheManager自定义器进行自定义配置
		return cacheManagerCustomizers.customize(builder.build());
	}

	private org.springframework.data.redis.cache.RedisCacheConfiguration determineConfiguration(
			CacheProperties cacheProperties,
			ObjectProvider<org.springframework.data.redis.cache.RedisCacheConfiguration> redisCacheConfiguration,
			ClassLoader classLoader) {
		return redisCacheConfiguration.getIfAvailable(() -> createConfiguration(cacheProperties, classLoader));
	}

	private org.springframework.data.redis.cache.RedisCacheConfiguration createConfiguration(
			CacheProperties cacheProperties, ClassLoader classLoader) {
		// spring.cache.redis配置
		Redis redisProperties = cacheProperties.getRedis();
		// 使用默认配置创建一个RedisCacheConfiguration实例
		org.springframework.data.redis.cache.RedisCacheConfiguration config = org.springframework.data.redis.cache.RedisCacheConfiguration
				.defaultCacheConfig();
		// 默认使用jdk的序列器
		config = config.serializeValuesWith(
				SerializationPair.fromSerializer(new JdkSerializationRedisSerializer(classLoader)));
		// spring.cache.redis.time-to-live配置
		if (redisProperties.getTimeToLive() != null) {
			config = config.entryTtl(redisProperties.getTimeToLive());
		}
		// spring.cache.redis.key-prefix配置
		if (redisProperties.getKeyPrefix() != null) {
			config = config.prefixKeysWith(redisProperties.getKeyPrefix());
		}
		// spring.cache.redis.cache-null-values配置
		if (!redisProperties.isCacheNullValues()) {
			config = config.disableCachingNullValues();
		}
		// spring.cache.redis.use-key-prefix配置
		if (!redisProperties.isUseKeyPrefix()) {
			config = config.disableKeyPrefix();
		}
		return config;
	}

}
