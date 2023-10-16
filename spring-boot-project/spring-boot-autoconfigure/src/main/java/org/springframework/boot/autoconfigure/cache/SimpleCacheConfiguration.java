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

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Simplest cache configuration, usually used as a fallback.
 *
 * 使用ConcurrentHashMap实现的简单的缓存配置
 * @author Stephane Nicoll
 */
@Configuration(proxyBeanMethods = false)
// 不能存在其他的CacheManager的Bean
@ConditionalOnMissingBean(CacheManager.class)
// 需要满足CacheCondition的条件
@Conditional(CacheCondition.class)
class SimpleCacheConfiguration {

	@Bean
	ConcurrentMapCacheManager cacheManager(CacheProperties cacheProperties,
			CacheManagerCustomizers cacheManagerCustomizers) {
		// 创建一个ConcurrentMapCacheManager实例
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
		List<String> cacheNames = cacheProperties.getCacheNames();
		if (!cacheNames.isEmpty()) {
			// 配置中配置的名字
			cacheManager.setCacheNames(cacheNames);
		}
		// 自定义配置
		return cacheManagerCustomizers.customize(cacheManager);
	}

}
