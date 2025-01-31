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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Mappings between {@link CacheType} and {@code @Configuration}.
 *
 * 缓存配置的映射
 *
 * @author Phillip Webb
 * @author Eddú Meléndez
 */
final class CacheConfigurations {

	private static final Map<CacheType, Class<?>> MAPPINGS;

	static {
		Map<CacheType, Class<?>> mappings = new EnumMap<>(CacheType.class);
		// 通用缓存
		mappings.put(CacheType.GENERIC, GenericCacheConfiguration.class);
		// ehCache
		mappings.put(CacheType.EHCACHE, EhCacheCacheConfiguration.class);
		// hazelcast
		mappings.put(CacheType.HAZELCAST, HazelcastCacheConfiguration.class);
		// infinispan
		mappings.put(CacheType.INFINISPAN, InfinispanCacheConfiguration.class);
		// jcache
		mappings.put(CacheType.JCACHE, JCacheCacheConfiguration.class);
		// couchbase
		mappings.put(CacheType.COUCHBASE, CouchbaseCacheConfiguration.class);
		// redis
		mappings.put(CacheType.REDIS, RedisCacheConfiguration.class);
		// caffeine
		mappings.put(CacheType.CAFFEINE, CaffeineCacheConfiguration.class);
		// simple
		mappings.put(CacheType.SIMPLE, SimpleCacheConfiguration.class);
		// 不开启缓存
		mappings.put(CacheType.NONE, NoOpCacheConfiguration.class);
		MAPPINGS = Collections.unmodifiableMap(mappings);
	}

	private CacheConfigurations() {
	}

	static String getConfigurationClass(CacheType cacheType) {
		Class<?> configurationClass = MAPPINGS.get(cacheType);
		Assert.state(configurationClass != null, () -> "Unknown cache type " + cacheType);
		return configurationClass.getName();
	}

	static CacheType getType(String configurationClassName) {
		for (Map.Entry<CacheType, Class<?>> entry : MAPPINGS.entrySet()) {
			if (entry.getValue().getName().equals(configurationClassName)) {
				return entry.getKey();
			}
		}
		throw new IllegalStateException("Unknown configuration class " + configurationClassName);
	}

}
