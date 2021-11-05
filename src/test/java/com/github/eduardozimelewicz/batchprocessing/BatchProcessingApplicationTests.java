package com.github.eduardozimelewicz.batchprocessing;

import com.github.eduardozimelewicz.batchprocessing.config.BatchConfig;
import com.github.eduardozimelewicz.batchprocessing.config.JobCompletionNotificationListener;
import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"feign.client.url=http://localhost:5555"})
@SpringBatchTest
@EnableAutoConfiguration
@EnableFeignClients
@ContextConfiguration(classes = {BatchConfig.class, JobCompletionNotificationListener.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
				DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWireMock(port = 5555)
class BatchProcessingApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(BatchProcessingApplicationTests.class);

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

  private static final HttpClient httpClient = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_1_1)
					.connectTimeout(Duration.ofSeconds(10))
					.build();

	@After
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions();
	}

	@Test
	public void testHttpConnection() throws Exception{
		stubFor(get(urlEqualTo("/ok"))
				.willReturn(ok()));
		HttpRequest request = HttpRequest.newBuilder()
						.GET()
						.uri(URI.create("http://localhost:5555/ok"))
						.setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
						.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		log.info(response.body());

		verify(getRequestedFor(urlEqualTo("/ok")).withHeader("User-Agent",
				equalTo("Java 11 HttpClient Bot")));
	}

	@Test
	public void batchExecutionCompletenessTest() throws Exception{
		stubFor(post(urlEqualTo("/batch"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("batch processed")));
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		JobInstance actualJobInstance = jobExecution.getJobInstance();
		ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

		assertThat(actualJobInstance.getJobName()).isEqualTo("importUserJob");
		assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
	}

	@Test
	public void batchExecutionStepCompletenessTest() throws Exception{
		stubFor(post(urlEqualTo("/batch"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("batch processed")));
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Collection<StepExecution> actualStepExecutions = jobExecution.getStepExecutions();
		ExitStatus actualExitStatus = jobExecution.getExitStatus();

		assertThat(actualStepExecutions.size()).isEqualTo(1);
		actualStepExecutions.forEach(stepExecution -> {
			// That's because it only has one step in the job
			assertThat(stepExecution.getStepName()).isEqualTo("step1");
			assertThat(stepExecution.getWriteCount()).isEqualTo(5);
			assertThat(stepExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		});
		assertThat(actualExitStatus.getExitCode()).isEqualTo("COMPLETED");
	}
}
