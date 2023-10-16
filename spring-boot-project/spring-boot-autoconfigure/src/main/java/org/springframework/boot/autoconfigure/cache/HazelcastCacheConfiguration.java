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

import java.io.IOException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastConfigResourceCondition;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Hazelcast cache configuration. Can either reuse the {@link HazelcastInstance} that has
 * been configured by the general {@link HazelcastAutoConfiguration} or create a separate
 * one if the {@code spring.cache.hazelcast.config} property has been set.
 * <p>
 * If the {@link HazelcastAutoConfiguration} has been disabled, an attempt to configure a
 * default {@link HazelcastInstance} is still made, using the same defaults.
 *
 * hazelcast缓存配置
 * @author Stephane Nicoll
 * @see HazelcastConfigResourceCondition
 */
@Configuration(proxyBeanMethods = false)
// 需要HazelcastInstance类存在并且需要HazelcastCacheManager类存在
@ConditionalOnClass({ HazelcastInstance.class, HazelcastCacheManager.class })
// 没有其他的CacheManager的Bean存在
@ConditionalOnMissingBean(CacheManager.class)
// 需要满足CacheCondition的条件
@Conditional(CacheCondition.class)
// 需要满足容器中只有一个HazelcastInstance的Bean
@ConditionalOnSingleCandidate(HazelcastInstance.class)
class HazelcastCacheConfiguration {

	/**
	 * 向容器中注册HazelcastCacheManager的Bean
	 * @param customizers
	 * @param existingHazelcastInstance
	 * @return
	 * @throws IOException
	 */
	@Bean
	HazelcastCacheManager cacheManager(CacheManagerCustomizers customizers, HazelcastInstance existingHazelcastInstance)
			throws IOException {
		// 创建HazelcastCacheManager实例
		HazelcastCacheManager cacheManager = new HazelcastCacheManager(existingHazelcastInstance);
		// 使用CacheManager自定义器进行自定义配置
		return customizers.customize(cacheManager);
	}

}
