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

package org.springframework.boot.autoconfigure.context;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for
 * {@link ConfigurationProperties @ConfigurationProperties} beans. Automatically binds and
 * validates any bean annotated with {@code @ConfigurationProperties}.
 *
 * 在解析自动配置的时候，会解析该类，会利用EnableConfigurationPropertiesRegistrar导入一些基础Bean：ConfigurationPropertiesBindingPostProcessor、
 * ConfigurationPropertiesBinder、ConfigurationPropertiesBeanDefinitionValidator、ConfigurationPropertiesBinder、ConfigurationBeanFactoryMetadata，
 * 并将@ConfigurationProperties注解的Bean进行解析并注册到容器中
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see EnableConfigurationProperties
 * @see ConfigurationProperties
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public class ConfigurationPropertiesAutoConfiguration {

}
