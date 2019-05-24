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

package org.springframework.boot.cli;

import java.io.File;
import java.net.URI;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests to exercise the samples.
 *
 * @author Dave Syer
 * @author Greg Turnquist
 * @author Roy Clarkson
 * @author Phillip Webb
 */
@ExtendWith(OutputCaptureExtension.class)
class SampleIntegrationTests {

	@RegisterExtension
	private CliTester cli;

	SampleIntegrationTests(CapturedOutput capturedOutput) {
		this.cli = new CliTester("samples/", capturedOutput);
	}

	@Test
	void retrySample() throws Exception {
		String output = this.cli.run("retry.groovy");
		URI scriptUri = new File("samples/retry.groovy").toURI();
		assertThat(output).contains("Hello World! From " + scriptUri);
	}

	@Test
	void beansSample() throws Exception {
		this.cli.run("beans.groovy");
		String output = this.cli.getHttpOutput();
		assertThat(output).contains("Hello World!");
	}

	@Test
	void templateSample() throws Exception {
		String output = this.cli.run("template.groovy");
		assertThat(output).contains("Hello World!");
	}

	@Test
	void jobSample() throws Exception {
		String output = this.cli.run("job.groovy", "foo=bar");
		assertThat(output).contains("completed with the following parameters");
	}

	@Test
	void jobWebSample() throws Exception {
		String output = this.cli.run("job.groovy", "web.groovy", "foo=bar");
		assertThat(output).contains("completed with the following parameters");
		String result = this.cli.getHttpOutput();
		assertThat(result).isEqualTo("World!");
	}

	@Test
	void webSample() throws Exception {
		this.cli.run("web.groovy");
		assertThat(this.cli.getHttpOutput()).isEqualTo("World!");
	}

	@Test
	void uiSample() throws Exception {
		this.cli.run("ui.groovy", "--classpath=.:src/test/resources");
		String result = this.cli.getHttpOutput();
		assertThat(result).contains("Hello World");
		result = this.cli.getHttpOutput("/css/bootstrap.min.css");
		assertThat(result).contains("container");
	}

	@Test
	void actuatorSample() throws Exception {
		this.cli.run("actuator.groovy");
		assertThat(this.cli.getHttpOutput()).isEqualTo("{\"message\":\"Hello World!\"}");
	}

	@Test
	void httpSample() throws Exception {
		String output = this.cli.run("http.groovy");
		assertThat(output).contains("Hello World");
	}

	@Test
	void integrationSample() throws Exception {
		String output = this.cli.run("integration.groovy");
		assertThat(output).contains("Hello, World");
	}

	@Test
	void xmlSample() throws Exception {
		String output = this.cli.run("runner.xml", "runner.groovy");
		assertThat(output).contains("Hello World");
	}

	@Test
	void txSample() throws Exception {
		String output = this.cli.run("tx.groovy");
		assertThat(output).contains("Foo count=");
	}

	@Test
	void jmsSample() throws Exception {
		System.setProperty("spring.artemis.embedded.queues", "spring-boot");
		try {
			String output = this.cli.run("jms.groovy");
			assertThat(output).contains("Received Greetings from Spring Boot via Artemis");
		}
		finally {
			System.clearProperty("spring.artemis.embedded.queues");
		}
	}

	@Test
	@Disabled("Requires RabbitMQ to be run, so disable it by default")
	void rabbitSample() throws Exception {
		String output = this.cli.run("rabbit.groovy");
		assertThat(output).contains("Received Greetings from Spring Boot via RabbitMQ");
	}

	@Test
	void caching() throws Exception {
		assertThat(this.cli.run("caching.groovy")).contains("Hello World");
	}

}
