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

import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheConfigurationImportSelector;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheManagerEntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.Assert;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the cache abstraction. Creates a
 * {@link CacheManager} if necessary when caching is enabled via
 * {@link EnableCaching @EnableCaching}.
 * <p>
 * Cache store can be auto-detected or specified explicitly via configuration.
 *
 * 缓存自动配置
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see EnableCaching
 */
@Configuration(proxyBeanMethods = false)
// 需要有CacheManager类存在
@ConditionalOnClass(CacheManager.class)
// 需要有CacheAspectSupport这个Bean存在，在@EnableCaching注解解析配置的时候，会注册一个CacheInterceptor到容器中，CacheInterceptor是CacheAspectSupport的子类
@ConditionalOnBean(CacheAspectSupport.class)
@ConditionalOnMissingBean(value = CacheManager.class, name = "cacheResolver")
// 导入spring.cache配置
@EnableConfigurationProperties(CacheProperties.class)
@AutoConfigureAfter({ CouchbaseAutoConfiguration.class, HazelcastAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class, RedisAutoConfiguration.class })
// CacheConfigurationImportSelector向容器中注册已知的支持的不同缓存的配置
@Import({ CacheConfigurationImportSelector.class, CacheManagerEntityManagerFactoryDependsOnPostProcessor.class })
public class CacheAutoConfiguration {

	/**
	 * 注册CacheManagerCustomizer定制器Bean到容器中
	 * @param customizers
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
		return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
	}

	/**
	 * 注册CacheManagerValidator验证器Bean到容器中
	 * @param cacheProperties
	 * @param cacheManager
	 * @return
	 */
	@Bean
	public CacheManagerValidator cacheAutoConfigurationValidator(CacheProperties cacheProperties,
			ObjectProvider<CacheManager> cacheManager) {
		return new CacheManagerValidator(cacheProperties, cacheManager);
	}

	@ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
	@ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
	static class CacheManagerEntityManagerFactoryDependsOnPostProcessor
			extends EntityManagerFactoryDependsOnPostProcessor {

		CacheManagerEntityManagerFactoryDependsOnPostProcessor() {
			super("cacheManager");
		}

	}

	/**
	 * Bean used to validate that a CacheManager exists and provide a more meaningful
	 * exception.
	 *
	 * CacheManager验证器，验证CacheManager存在
	 */
	static class CacheManagerValidator implements InitializingBean {

		private final CacheProperties cacheProperties;

		private final ObjectProvider<CacheManager> cacheManager;

		CacheManagerValidator(CacheProperties cacheProperties, ObjectProvider<CacheManager> cacheManager) {
			this.cacheProperties = cacheProperties;
			this.cacheManager = cacheManager;
		}

		@Override
		public void afterPropertiesSet() {
			Assert.notNull(this.cacheManager.getIfAvailable(),
					() -> "No cache manager could be auto-configured, check your configuration (caching " + "type is '"
							+ this.cacheProperties.getType() + "')");
		}

	}

	/**
	 * {@link ImportSelector} to add {@link CacheType} configuration classes.
	 * 导入所有支持的不同缓存的配置
	 */
	static class CacheConfigurationImportSelector implements ImportSelector {

		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			CacheType[] types = CacheType.values();
			String[] imports = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				imports[i] = CacheConfigurations.getConfigurationClass(types[i]);
			}
			return imports;
		}

	}

}
