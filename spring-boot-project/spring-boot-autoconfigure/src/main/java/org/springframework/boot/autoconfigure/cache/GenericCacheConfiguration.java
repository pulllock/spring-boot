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

import java.util.Collection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Generic cache configuration based on arbitrary {@link Cache} instances defined in the
 * context.
 *
 * 通用缓存配置
 *
 * @author Stephane Nicoll
 */
@Configuration(proxyBeanMethods = false)
// 需要有Cache实现类的Bean存在
@ConditionalOnBean(Cache.class)
// 没有其他的CacheManager的Bean存在
@ConditionalOnMissingBean(CacheManager.class)
// 需要满足CacheCondition中的条件
@Conditional(CacheCondition.class)
class GenericCacheConfiguration {

	@Bean
	SimpleCacheManager cacheManager(CacheManagerCustomizers customizers, Collection<Cache> caches) {
		// 创建一个简单的CacheManager实例
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(caches);
		// 执行CacheManager自定义器对CacheManager进行自定义
		return customizers.customize(cacheManager);
	}

}
