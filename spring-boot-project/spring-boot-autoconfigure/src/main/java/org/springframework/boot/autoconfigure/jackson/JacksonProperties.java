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

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * Configuration properties to configure Jackson.
 *
 * @author Andy Wilkinson
 * @author Marcel Overdijk
 * @author Johannes Edmeier
 * @since 1.2.0
 *
 * 可以对Jackson进行配置的属性
 */
@ConfigurationProperties(prefix = "spring.jackson")
public class JacksonProperties {

	/**
	 * Date format string or a fully-qualified date format class name. For instance,
	 * `yyyy-MM-dd HH:mm:ss`.
	 *
	 * 日期格式
	 */
	private String dateFormat;

	/**
	 * Joda date time format string. If not configured, "date-format" is used as a
	 * fallback if it is configured with a format string.
	 *
	 * Joda日期格式
	 */
	private String jodaDateTimeFormat;

	/**
	 * One of the constants on Jackson's PropertyNamingStrategy. Can also be a
	 * fully-qualified class name of a PropertyNamingStrategy subclass.
	 *
	 * 指定Jackson的属性命名策略
	 */
	private String propertyNamingStrategy;

	/**
	 * Jackson visibility thresholds that can be used to limit which methods (and fields)
	 * are auto-detected.
	 *
	 * 指定字段的访问级别
	 */
	private final Map<PropertyAccessor, JsonAutoDetect.Visibility> visibility = new EnumMap<>(PropertyAccessor.class);

	/**
	 * Jackson on/off features that affect the way Java objects are serialized.
	 *
	 * 配置序列化的特性
	 */
	private final Map<SerializationFeature, Boolean> serialization = new EnumMap<>(SerializationFeature.class);

	/**
	 * Jackson on/off features that affect the way Java objects are deserialized.
	 *
	 * 配置反序列化的特性
	 */
	private final Map<DeserializationFeature, Boolean> deserialization = new EnumMap<>(DeserializationFeature.class);

	/**
	 * Jackson general purpose on/off features.
	 *
	 * 配置Jackson通用的特性
	 */
	private final Map<MapperFeature, Boolean> mapper = new EnumMap<>(MapperFeature.class);

	/**
	 * Jackson on/off features for parsers.
	 *
	 * 配置Jackson解析器的特性
	 */
	private final Map<JsonParser.Feature, Boolean> parser = new EnumMap<>(JsonParser.Feature.class);

	/**
	 * Jackson on/off features for generators.
	 *
	 * 配置Jackson的JsonGenerator的特性
	 */
	private final Map<JsonGenerator.Feature, Boolean> generator = new EnumMap<>(JsonGenerator.Feature.class);

	/**
	 * Controls the inclusion of properties during serialization. Configured with one of
	 * the values in Jackson's JsonInclude.Include enumeration.
	 *
	 * 配置序列化时属性的包含范围
	 */
	private JsonInclude.Include defaultPropertyInclusion;

	/**
	 * Time zone used when formatting dates. For instance, "America/Los_Angeles" or
	 * "GMT+10".
	 *
	 * 配置时区
	 */
	private TimeZone timeZone = null;

	/**
	 * Locale used for formatting.
	 *
	 * 配置本地化
	 */
	private Locale locale;

	public String getDateFormat() {
		return this.dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Deprecated
	@DeprecatedConfigurationProperty(replacement = "dateFormat",
			reason = "Auto-configuration for Jackson's Joda-Time integration is "
					+ "deprecated in favor of its Java 8 Time integration")
	public String getJodaDateTimeFormat() {
		return this.jodaDateTimeFormat;
	}

	public void setJodaDateTimeFormat(String jodaDataTimeFormat) {
		this.jodaDateTimeFormat = jodaDataTimeFormat;
	}

	public String getPropertyNamingStrategy() {
		return this.propertyNamingStrategy;
	}

	public void setPropertyNamingStrategy(String propertyNamingStrategy) {
		this.propertyNamingStrategy = propertyNamingStrategy;
	}

	public Map<PropertyAccessor, JsonAutoDetect.Visibility> getVisibility() {
		return this.visibility;
	}

	public Map<SerializationFeature, Boolean> getSerialization() {
		return this.serialization;
	}

	public Map<DeserializationFeature, Boolean> getDeserialization() {
		return this.deserialization;
	}

	public Map<MapperFeature, Boolean> getMapper() {
		return this.mapper;
	}

	public Map<JsonParser.Feature, Boolean> getParser() {
		return this.parser;
	}

	public Map<JsonGenerator.Feature, Boolean> getGenerator() {
		return this.generator;
	}

	public JsonInclude.Include getDefaultPropertyInclusion() {
		return this.defaultPropertyInclusion;
	}

	public void setDefaultPropertyInclusion(JsonInclude.Include defaultPropertyInclusion) {
		this.defaultPropertyInclusion = defaultPropertyInclusion;
	}

	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
