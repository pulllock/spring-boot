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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ResourceCondition;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * EhCache cache configuration. Only kick in if a configuration file location is set or if
 * a default configuration file exists.
 *
 * ehCache缓存配置
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Madhura Bhave
 */
@Configuration(proxyBeanMethods = false)
// 需要有Cache实现类存在，并且需要有EhCacheCacheManager类存在
@ConditionalOnClass({ Cache.class, EhCacheCacheManager.class })
// 不存在其他的CacheManager的Bean
@ConditionalOnMissingBean(org.springframework.cache.CacheManager.class)
// 需要满足CacheCondition条件并且需要满足ehCache的配置存在
@Conditional({ CacheCondition.class, EhCacheCacheConfiguration.ConfigAvailableCondition.class })
class EhCacheCacheConfiguration {

	/**
	 * 向容器中注册EhCacheCacheManager的Bean
	 * @param customizers
	 * @param ehCacheCacheManager
	 * @return
	 */
	@Bean
	EhCacheCacheManager cacheManager(CacheManagerCustomizers customizers, CacheManager ehCacheCacheManager) {
		// 创建EhCacheCacheManager实例
		// 使用CacheManagerCustomizer对EhCacheManager进行自定义配置
		return customizers.customize(new EhCacheCacheManager(ehCacheCacheManager));
	}

	/**
	 * 向容器中注册EhCacheCacheManager的Bean
	 * @param cacheProperties
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	CacheManager ehCacheCacheManager(CacheProperties cacheProperties) {
		// 获取spring.cache.ehcache.config配置的配置文件位置
		Resource location = cacheProperties.resolveConfigLocation(cacheProperties.getEhcache().getConfig());
		if (location != null) {
			// 通过配置文件创建EhCacheCacheManager
			return EhCacheManagerUtils.buildCacheManager(location);
		}
		// 使用默认位置处的配置文件创建EhCacheCacheManager
		return EhCacheManagerUtils.buildCacheManager();
	}

	/**
	 * Determine if the EhCache configuration is available. This either kick in if a
	 * default configuration has been found or if property referring to the file to use
	 * has been set.
	 * 检查EhCache配置是否存在
	 */
	static class ConfigAvailableCondition extends ResourceCondition {

		ConfigAvailableCondition() {
			super("EhCache", "spring.cache.ehcache.config", "classpath:/ehcache.xml");
		}

	}

}
