package com.github.eduardozimelewicz.batchprocessing.config;

import com.github.eduardozimelewicz.batchprocessing.client.PostApiFeignClient;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

  private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

  @Autowired
  private PostApiFeignClient postApiFeignClient;

  @Override
  public void afterJob(JobExecution jobExecution) {
    if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
      log.info("!!! JOB FINISHED !!! Sending data to API");
      try {
        File batchJsonResultFile = findLatestGeneratedBatchFile();
        String json = readFileAsString(batchJsonResultFile);
        Response response = postApiFeignClient.sendBatchJobResults(json);
        log.info("job_id: " + jobExecution.getId() + ", status: " + response.status());
      } catch (IOException e){
        e.printStackTrace();
        log.error("Json file read from Job: " + jobExecution.getId() + " failed");
      }
    } else {
      log.error("Job execution " + jobExecution.getId() + " failed");
    }
  }

  public static File findLatestGeneratedBatchFile() {
    File batchDir = new File("/tmp/batch");
      if(batchDir.isDirectory()) {
        Optional<File> opFile = Arrays.stream(batchDir.listFiles(File::isFile)).max(
                (f1,f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        if(opFile.isPresent()){
          return opFile.get();
        }
      }
      return null;
  }

  public static String readFileAsString(File file) throws IOException {
    return new String(Files.readAllBytes(Paths.get(file.getPath())));
  }
}