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

package org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusPushGatewayManager;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PrometheusMetricsExportAutoConfiguration}.
 *
 * @author Andy Wilkinson
 */
public class PrometheusMetricsExportAutoConfigurationTests {

	@Rule
	public final OutputCapture output = new OutputCapture();

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations
					.of(PrometheusMetricsExportAutoConfiguration.class));

	@Test
	public void backsOffWithoutAClock() {
		this.contextRunner.run((context) -> assertThat(context)
				.doesNotHaveBean(PrometheusMeterRegistry.class));
	}

	@Test
	public void autoConfiguresItsConfigCollectorRegistryAndMeterRegistry() {
		this.contextRunner.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(PrometheusMeterRegistry.class)
						.hasSingleBean(CollectorRegistry.class)
						.hasSingleBean(PrometheusConfig.class));
	}

	@Test
	public void autoConfigurationCanBeDisabled() {
		this.contextRunner
				.withPropertyValues("management.metrics.export.prometheus.enabled=false")
				.run((context) -> assertThat(context)
						.doesNotHaveBean(PrometheusMeterRegistry.class)
						.doesNotHaveBean(CollectorRegistry.class)
						.doesNotHaveBean(PrometheusConfig.class));
	}

	@Test
	public void allowsCustomConfigToBeUsed() {
		this.contextRunner.withUserConfiguration(CustomConfigConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(PrometheusMeterRegistry.class)
						.hasSingleBean(CollectorRegistry.class)
						.hasSingleBean(PrometheusConfig.class).hasBean("customConfig"));
	}

	@Test
	public void allowsCustomRegistryToBeUsed() {
		this.contextRunner.withUserConfiguration(CustomRegistryConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(PrometheusMeterRegistry.class)
						.hasBean("customRegistry").hasSingleBean(CollectorRegistry.class)
						.hasSingleBean(PrometheusConfig.class));
	}

	@Test
	public void allowsCustomCollectorRegistryToBeUsed() {
		this.contextRunner
				.withUserConfiguration(CustomCollectorRegistryConfiguration.class)
				.run((context) -> assertThat(context)
						.hasSingleBean(PrometheusMeterRegistry.class)
						.hasBean("customCollectorRegistry")
						.hasSingleBean(CollectorRegistry.class)
						.hasSingleBean(PrometheusConfig.class));
	}

	@Test
	public void addsScrapeEndpointToManagementContext() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withUserConfiguration(BaseConfiguration.class)
				.withPropertyValues(
						"management.endpoints.web.exposure.include=prometheus")
				.run((context) -> assertThat(context)
						.hasSingleBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	public void scrapeEndpointNotAddedToManagementContextWhenNotExposed() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context)
						.doesNotHaveBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	public void scrapeEndpointCanBeDisabled() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues(
						"management.endpoints.web.exposure.include=prometheus")
				.withPropertyValues("management.endpoint.prometheus.enabled=false")
				.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> assertThat(context)
						.doesNotHaveBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	public void allowsCustomScrapeEndpointToBeUsed() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withUserConfiguration(CustomEndpointConfiguration.class)
				.run((context) -> assertThat(context).hasBean("customEndpoint")
						.hasSingleBean(PrometheusScrapeEndpoint.class));
	}

	@Test
	public void withPushGatewayEnabled() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues(
						"management.metrics.export.prometheus.pushgateway.enabled=true")
				.withUserConfiguration(BaseConfiguration.class).run((context) -> {
					assertThat(this.output.toString())
							.doesNotContain("Invalid PushGateway base url");
					hasGatewayURL(context, "http://localhost:9091/metrics/job/");
				});
	}

	@Test
	@Deprecated
	public void withCustomLegacyPushGatewayURL() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues(
						"management.metrics.export.prometheus.pushgateway.enabled=true",
						"management.metrics.export.prometheus.pushgateway.base-url=localhost:9090")
				.withUserConfiguration(BaseConfiguration.class).run((context) -> {
					assertThat(this.output.toString())
							.contains("Invalid PushGateway base url")
							.contains("localhost:9090");
					hasGatewayURL(context, "http://localhost:9090/metrics/job/");
				});
	}

	@Test
	public void withCustomPushGatewayURL() {
		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(ManagementContextAutoConfiguration.class))
				.withPropertyValues(
						"management.metrics.export.prometheus.pushgateway.enabled=true",
						"management.metrics.export.prometheus.pushgateway.base-url=https://example.com:8080")
				.withUserConfiguration(BaseConfiguration.class)
				.run((context) -> hasGatewayURL(context,
						"https://example.com:8080/metrics/job/"));
	}

	private void hasGatewayURL(AssertableApplicationContext context, String url) {
		assertThat(context).hasSingleBean(PrometheusPushGatewayManager.class);
		PrometheusPushGatewayManager gatewayManager = context
				.getBean(PrometheusPushGatewayManager.class);
		Object pushGateway = ReflectionTestUtils.getField(gatewayManager, "pushGateway");
		assertThat(pushGateway).hasFieldOrPropertyWithValue("gatewayBaseURL", url);
	}

	@Configuration(proxyBeanMethods = false)
	static class BaseConfiguration {

		@Bean
		public Clock clock() {
			return Clock.SYSTEM;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomConfigConfiguration {

		@Bean
		public PrometheusConfig customConfig() {
			return (key) -> null;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomRegistryConfiguration {

		@Bean
		public PrometheusMeterRegistry customRegistry(PrometheusConfig config,
				CollectorRegistry collectorRegistry, Clock clock) {
			return new PrometheusMeterRegistry(config, collectorRegistry, clock);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomCollectorRegistryConfiguration {

		@Bean
		public CollectorRegistry customCollectorRegistry() {
			return new CollectorRegistry();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@Import(BaseConfiguration.class)
	static class CustomEndpointConfiguration {

		@Bean
		public PrometheusScrapeEndpoint customEndpoint(
				CollectorRegistry collectorRegistry) {
			return new PrometheusScrapeEndpoint(collectorRegistry);
		}

	}

}
