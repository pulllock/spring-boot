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
import java.io.InputStream;
import java.util.List;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

/**
 * Infinispan cache configuration.
 *
 * infinispan缓存配置
 *
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Raja Kolli
 * @since 1.3.0
 */
@Configuration(proxyBeanMethods = false)
// 需要有SpringEmbeddedCacheManager类存在
@ConditionalOnClass(SpringEmbeddedCacheManager.class)
// 没有其他的CacheManager的Bean存在
@ConditionalOnMissingBean(CacheManager.class)
// 需要满足CacheCondition条件
@Conditional(CacheCondition.class)
public class InfinispanCacheConfiguration {

	/**
	 * 向容器中注册一个SpringEmbeddedCacheManager的Bean
	 * @param customizers
	 * @param embeddedCacheManager
	 * @return
	 */
	@Bean
	public SpringEmbeddedCacheManager cacheManager(CacheManagerCustomizers customizers,
			EmbeddedCacheManager embeddedCacheManager) {
		// 创建SpringEmbeddedCacheManager实例
		SpringEmbeddedCacheManager cacheManager = new SpringEmbeddedCacheManager(embeddedCacheManager);
		// 使用CacheManager自定义器进行自定义配置
		return customizers.customize(cacheManager);
	}

	/**
	 * 如果容器中不存在相关的Bean，就注册一个Infinispan的CacheManager的Bean
	 * @param cacheProperties
	 * @param defaultConfigurationBuilder
	 * @return
	 * @throws IOException
	 */
	@Bean(destroyMethod = "stop")
	@ConditionalOnMissingBean
	public EmbeddedCacheManager infinispanCacheManager(CacheProperties cacheProperties,
			ObjectProvider<ConfigurationBuilder> defaultConfigurationBuilder) throws IOException {
		// 创建一个infinispan的CacheManager
		EmbeddedCacheManager cacheManager = createEmbeddedCacheManager(cacheProperties);
		// 配置的获取缓存名字
		List<String> cacheNames = cacheProperties.getCacheNames();
		if (!CollectionUtils.isEmpty(cacheNames)) {
			// 给每个缓存创建对应的配置
			cacheNames.forEach((cacheName) -> cacheManager.defineConfiguration(cacheName,
					getDefaultCacheConfiguration(defaultConfigurationBuilder.getIfAvailable())));
		}
		return cacheManager;
	}

	private EmbeddedCacheManager createEmbeddedCacheManager(CacheProperties cacheProperties) throws IOException {
		// spring.cache.infinispan.config指定的配置文件位置
		Resource location = cacheProperties.resolveConfigLocation(cacheProperties.getInfinispan().getConfig());
		if (location != null) {
			try (InputStream in = location.getInputStream()) {
				// 根据指定的位置创建infinispan的CacheManager
				return new DefaultCacheManager(in);
			}
		}
		// 使用默认的配置创建infinispan的CacheManager
		return new DefaultCacheManager();
	}

	private org.infinispan.configuration.cache.Configuration getDefaultCacheConfiguration(
			ConfigurationBuilder defaultConfigurationBuilder) {
		if (defaultConfigurationBuilder != null) {
			return defaultConfigurationBuilder.build();
		}
		return new ConfigurationBuilder().build();
	}

}
