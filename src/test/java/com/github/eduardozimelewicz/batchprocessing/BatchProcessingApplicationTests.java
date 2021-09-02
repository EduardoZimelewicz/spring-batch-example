package com.github.eduardozimelewicz.batchprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.eduardozimelewicz.batchprocessing.config.BatchConfig;
import com.github.eduardozimelewicz.batchprocessing.config.JobCompletionNotificationListener;
import org.junit.After;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import java.util.Collection;

@RunWith(SpringRunner.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {BatchConfig.class, JobCompletionNotificationListener.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
				DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BatchProcessingApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@After
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions();
	}

	@Test
	public void batchExecutionCompletenessTest() throws Exception{
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		JobInstance actualJobInstance = jobExecution.getJobInstance();
		ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

		assertThat(actualJobInstance.getJobName()).isEqualTo("importUserJob");
		assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
	}

	@Test
	public void batchExecutionStepCompletenessTest() throws Exception{
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
