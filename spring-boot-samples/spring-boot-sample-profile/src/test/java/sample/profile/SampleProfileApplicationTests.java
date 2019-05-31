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

package sample.profile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.boot.test.extension.CapturedOutput;
import org.springframework.boot.test.extension.OutputExtension;

import static org.assertj.core.api.Assertions.assertThat;

class SampleProfileApplicationTests {

	@RegisterExtension
	CapturedOutput output = OutputExtension.capture();

	private String profiles;

	@BeforeEach
	public void before() {
		this.profiles = System.getProperty("spring.profiles.active");
	}

	@AfterEach
	public void after() {
		if (this.profiles != null) {
			System.setProperty("spring.profiles.active", this.profiles);
		}
		else {
			System.clearProperty("spring.profiles.active");
		}
	}

	@Test
	void testDefaultProfile() throws Exception {
		SampleProfileApplication.main(new String[0]);
		assertThat(this.output).contains("Hello Phil");
	}

	@Test
	void testGoodbyeProfile() throws Exception {
		System.setProperty("spring.profiles.active", "goodbye");
		SampleProfileApplication.main(new String[0]);
		assertThat(this.output).contains("Goodbye Everyone");
	}

	@Test
	void testGenericProfile() throws Exception {
		/*
		 * This is a profile that requires a new environment property, and one which is
		 * only overridden in the current working directory. That file also only contains
		 * partial overrides, and the default application.yml should still supply the
		 * "name" property.
		 */
		System.setProperty("spring.profiles.active", "generic");
		SampleProfileApplication.main(new String[0]);
		assertThat(this.output).contains("Bonjour Phil");
	}

	@Test
	void testGoodbyeProfileFromCommandline() throws Exception {
		SampleProfileApplication
				.main(new String[] { "--spring.profiles.active=goodbye" });
		assertThat(this.output).contains("Goodbye Everyone");
	}

}
