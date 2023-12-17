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

package org.springframework.boot.autoconfigure.jackson;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Auto configuration for Jackson. The following auto-configuration will get applied:
 * <ul>
 * <li>an {@link ObjectMapper} in case none is already configured.</li>
 * <li>a {@link Jackson2ObjectMapperBuilder} in case none is already configured.</li>
 * <li>auto-registration for all {@link Module} beans with all {@link ObjectMapper} beans
 * (including the defaulted ones).</li>
 * </ul>
 *
 * @author Oliver Gierke
 * @author Andy Wilkinson
 * @author Marcel Overdijk
 * @author Sebastien Deleuze
 * @author Johannes Edmeier
 * @author Phillip Webb
 * @author Eddú Meléndez
 * @since 1.1.0
 *
 * Jackson的自动配置
 */
@Configuration(proxyBeanMethods = false)
// ObjectMapper存在
@ConditionalOnClass(ObjectMapper.class)
public class JacksonAutoConfiguration {

	/**
	 * 默认的特性
	 */
	private static final Map<?, Boolean> FEATURE_DEFAULTS;

	static {
		Map<Object, Boolean> featureDefaults = new HashMap<>();
		// 序列化时日期转换为时间戳，设置为false，WRITE_DATES_AS_TIMESTAMPS默认为true
		featureDefaults.put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		FEATURE_DEFAULTS = Collections.unmodifiableMap(featureDefaults);
	}

	@Bean
	public JsonComponentModule jsonComponentModule() {
		return new JsonComponentModule();
	}

	@Configuration(proxyBeanMethods = false)
	// Jackson2ObjectMapperBuilder类存在
	@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
	static class JacksonObjectMapperConfiguration {

		@Bean
		@Primary
		@ConditionalOnMissingBean
		ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
			// 使用Jackson2ObjectMapperBuilder创建ObjectMapper
			return builder.createXmlMapper(false).build();
		}

	}

	@Deprecated
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ Jackson2ObjectMapperBuilder.class, DateTime.class, DateTimeSerializer.class,
			JacksonJodaDateFormat.class })
	static class JodaDateTimeJacksonConfiguration {

		private static final Log logger = LogFactory.getLog(JodaDateTimeJacksonConfiguration.class);

		@Bean
		SimpleModule jodaDateTimeSerializationModule(JacksonProperties jacksonProperties) {
			logger.warn("Auto-configuration of Jackson's Joda-Time integration is deprecated in favor of using "
					+ "java.time (JSR-310).");
			SimpleModule module = new SimpleModule();
			JacksonJodaDateFormat jacksonJodaFormat = getJacksonJodaDateFormat(jacksonProperties);
			if (jacksonJodaFormat != null) {
				module.addSerializer(DateTime.class, new DateTimeSerializer(jacksonJodaFormat, 0));
			}
			return module;
		}

		private JacksonJodaDateFormat getJacksonJodaDateFormat(JacksonProperties jacksonProperties) {
			if (jacksonProperties.getJodaDateTimeFormat() != null) {
				return new JacksonJodaDateFormat(
						DateTimeFormat.forPattern(jacksonProperties.getJodaDateTimeFormat()).withZoneUTC());
			}
			if (jacksonProperties.getDateFormat() != null) {
				try {
					return new JacksonJodaDateFormat(
							DateTimeFormat.forPattern(jacksonProperties.getDateFormat()).withZoneUTC());
				}
				catch (IllegalArgumentException ex) {
					if (logger.isWarnEnabled()) {
						logger.warn("spring.jackson.date-format could not be used to "
								+ "configure formatting of Joda's DateTime. You may want "
								+ "to configure spring.jackson.joda-date-time-format as well.");
					}
				}
			}
			return null;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(ParameterNamesModule.class)
	static class ParameterNamesModuleConfiguration {

		@Bean
		@ConditionalOnMissingBean
		ParameterNamesModule parameterNamesModule() {
			return new ParameterNamesModule(JsonCreator.Mode.DEFAULT);
		}

	}

	/**
	 * Jackson2ObjectMapperBuilder配置
	 */
	@Configuration(proxyBeanMethods = false)
	// Jackson2ObjectMapperBuilder类存在
	@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
	static class JacksonObjectMapperBuilderConfiguration {

		/**
		 * 创建Jackson2ObjectMapperBuilder的Bean
		 * @param applicationContext
		 * @param customizers
		 * @return
		 */
		@Bean
		@Scope("prototype")
		@ConditionalOnMissingBean
		Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder(ApplicationContext applicationContext,
				List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
			// 创建Jackson2ObjectMapperBuilder
			Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
			// 设置上下文
			builder.applicationContext(applicationContext);
			// 自定义Jackson2ObjectMapperBuilder
			customize(builder, customizers);
			return builder;
		}

		private void customize(Jackson2ObjectMapperBuilder builder,
				List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
			// 遍历Jackson2ObjectMapperBuilderCustomizer，对Jackson2ObjectMapperBuilder进行自定义设置
			for (Jackson2ObjectMapperBuilderCustomizer customizer : customizers) {
				customizer.customize(builder);
			}
		}

	}

	/**
	 * Jackson2ObjectMapperBuilderCustomizer配置
	 */
	@Configuration(proxyBeanMethods = false)
	// Jackson2ObjectMapperBuilder类存在
	@ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
	@EnableConfigurationProperties(JacksonProperties.class)
	static class Jackson2ObjectMapperBuilderCustomizerConfiguration {

		/**
		 * 创建标准的Jackson2ObjectMapperBuilderCustomizer实例
		 * @param applicationContext
		 * @param jacksonProperties
		 * @return
		 */
		@Bean
		StandardJackson2ObjectMapperBuilderCustomizer standardJacksonObjectMapperBuilderCustomizer(
				ApplicationContext applicationContext, JacksonProperties jacksonProperties) {
			return new StandardJackson2ObjectMapperBuilderCustomizer(applicationContext, jacksonProperties);
		}

		/**
		 * Jackson2ObjectMapperBuilderCustomizer的标准实现
		 */
		static final class StandardJackson2ObjectMapperBuilderCustomizer
				implements Jackson2ObjectMapperBuilderCustomizer, Ordered {

			/**
			 * 应用上下文
			 */
			private final ApplicationContext applicationContext;

			/**
			 * Jackson配置属性
			 */
			private final JacksonProperties jacksonProperties;

			StandardJackson2ObjectMapperBuilderCustomizer(ApplicationContext applicationContext,
					JacksonProperties jacksonProperties) {
				this.applicationContext = applicationContext;
				this.jacksonProperties = jacksonProperties;
			}

			@Override
			public int getOrder() {
				return 0;
			}

			/**
			 * 对Jackson2ObjectMapperBuilder进行自定义
			 * @param builder the JacksonObjectMapperBuilder to customize
			 */
			@Override
			public void customize(Jackson2ObjectMapperBuilder builder) {

				// 设置序列化的Include
				if (this.jacksonProperties.getDefaultPropertyInclusion() != null) {
					builder.serializationInclusion(this.jacksonProperties.getDefaultPropertyInclusion());
				}
				// 设置时区
				if (this.jacksonProperties.getTimeZone() != null) {
					builder.timeZone(this.jacksonProperties.getTimeZone());
				}
				// 设置默认特性
				configureFeatures(builder, FEATURE_DEFAULTS);
				// 设置visibility
				configureVisibility(builder, this.jacksonProperties.getVisibility());
				// 设置反序列化特性
				configureFeatures(builder, this.jacksonProperties.getDeserialization());
				// 设置序列化特性
				configureFeatures(builder, this.jacksonProperties.getSerialization());
				// 设置Jackson通用特性
				configureFeatures(builder, this.jacksonProperties.getMapper());
				// 设置parser特性
				configureFeatures(builder, this.jacksonProperties.getParser());
				// 设置generator特性
				configureFeatures(builder, this.jacksonProperties.getGenerator());
				// 配置日期格式
				configureDateFormat(builder);
				// 配置属性命名策略
				configurePropertyNamingStrategy(builder);
				// 配置模块，会将所有的Module类型的Bean注册到Jackson2ObjectMapperBuilder中
				configureModules(builder);
				// 配置本地化
				configureLocale(builder);
			}

			private void configureFeatures(Jackson2ObjectMapperBuilder builder, Map<?, Boolean> features) {
				features.forEach((feature, value) -> {
					if (value != null) {
						if (value) {
							builder.featuresToEnable(feature);
						}
						else {
							builder.featuresToDisable(feature);
						}
					}
				});
			}

			private void configureVisibility(Jackson2ObjectMapperBuilder builder,
					Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilities) {
				visibilities.forEach(builder::visibility);
			}

			private void configureDateFormat(Jackson2ObjectMapperBuilder builder) {
				// We support a fully qualified class name extending DateFormat or a date
				// pattern string value
				// 配置的日期格式
				String dateFormat = this.jacksonProperties.getDateFormat();
				if (dateFormat != null) {
					try {
						// 可以配置类名字
						Class<?> dateFormatClass = ClassUtils.forName(dateFormat, null);
						builder.dateFormat((DateFormat) BeanUtils.instantiateClass(dateFormatClass));
					}
					catch (ClassNotFoundException ex) {
						// 配置的日期格式
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
						// Since Jackson 2.6.3 we always need to set a TimeZone (see
						// gh-4170). If none in our properties fallback to the Jackson's
						// default
						// 配置的时区
						TimeZone timeZone = this.jacksonProperties.getTimeZone();
						if (timeZone == null) {
							// 默认的时区
							timeZone = new ObjectMapper().getSerializationConfig().getTimeZone();
						}
						simpleDateFormat.setTimeZone(timeZone);
						builder.dateFormat(simpleDateFormat);
					}
				}
			}

			private void configurePropertyNamingStrategy(Jackson2ObjectMapperBuilder builder) {
				// We support a fully qualified class name extending Jackson's
				// PropertyNamingStrategy or a string value corresponding to the constant
				// names in PropertyNamingStrategy which hold default provided
				// implementations
				// 配置的属性命名策略
				String strategy = this.jacksonProperties.getPropertyNamingStrategy();
				if (strategy != null) {
					try {
						configurePropertyNamingStrategyClass(builder, ClassUtils.forName(strategy, null));
					}
					catch (ClassNotFoundException ex) {
						configurePropertyNamingStrategyField(builder, strategy);
					}
				}
			}

			private void configurePropertyNamingStrategyClass(Jackson2ObjectMapperBuilder builder,
					Class<?> propertyNamingStrategyClass) {
				builder.propertyNamingStrategy(
						(PropertyNamingStrategy) BeanUtils.instantiateClass(propertyNamingStrategyClass));
			}

			private void configurePropertyNamingStrategyField(Jackson2ObjectMapperBuilder builder, String fieldName) {
				// Find the field (this way we automatically support new constants
				// that may be added by Jackson in the future)
				Field field = ReflectionUtils.findField(PropertyNamingStrategy.class, fieldName,
						PropertyNamingStrategy.class);
				Assert.notNull(field, () -> "Constant named '" + fieldName + "' not found on "
						+ PropertyNamingStrategy.class.getName());
				try {
					builder.propertyNamingStrategy((PropertyNamingStrategy) field.get(null));
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			}

			private void configureModules(Jackson2ObjectMapperBuilder builder) {
				Collection<Module> moduleBeans = getBeans(this.applicationContext, Module.class);
				builder.modulesToInstall(moduleBeans.toArray(new Module[0]));
			}

			private void configureLocale(Jackson2ObjectMapperBuilder builder) {
				// 配置的本地化信息
				Locale locale = this.jacksonProperties.getLocale();
				if (locale != null) {
					builder.locale(locale);
				}
			}

			private static <T> Collection<T> getBeans(ListableBeanFactory beanFactory, Class<T> type) {
				return BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type).values();
			}

		}

	}

}
