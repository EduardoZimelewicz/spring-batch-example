package com.github.eduardozimelewicz.batchprocessing.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JobController {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job job;

  @GetMapping("/launch")
  public ResponseEntity<String> launchBatchJob() throws Exception {
    JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
    return new ResponseEntity<>("job_id: " + jobExecution.getId() + " launched", HttpStatus.CREATED);
  }
}
