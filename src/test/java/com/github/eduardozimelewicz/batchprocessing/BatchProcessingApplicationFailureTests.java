package com.github.eduardozimelewicz.batchprocessing;

import com.github.eduardozimelewicz.batchprocessing.config.BatchConfig;
import com.github.eduardozimelewicz.batchprocessing.config.JobCompletionNotificationListener;
import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"feign.client.url=http://localhost:5555", "spring.datasource.data:classpath:wrong.sql"})
@SpringBatchTest
@EnableAutoConfiguration
@EnableFeignClients
@ContextConfiguration(classes = {BatchConfig.class, JobCompletionNotificationListener.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureWireMock(port = 5555)
public class BatchProcessingApplicationFailureTests {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private JobRepositoryTestUtils jobRepositoryTestUtils;

  @After
  public void cleanUp() {
    jobRepositoryTestUtils.removeJobExecutions();
  }

  @Test
  public void batchExecutionFailedTest() throws Exception {
    stubFor(post(urlEqualTo("/batch"))
        .willReturn(aResponse()
            .withStatus(500)));
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();
    JobInstance actualJobInstance = jobExecution.getJobInstance();
    ExitStatus actualExitStatus = jobExecution.getExitStatus();

    assertThat(actualJobInstance.getJobName()).isEqualTo("importUserJob");
    assertThat(actualExitStatus.getExitCode()).isEqualTo("FAILED");
  }
}
