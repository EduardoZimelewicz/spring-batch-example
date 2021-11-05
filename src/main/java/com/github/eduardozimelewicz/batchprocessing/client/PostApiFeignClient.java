package com.github.eduardozimelewicz.batchprocessing.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "python-api", url = "${feign.client.url}")
public interface PostApiFeignClient {
  @PostMapping(value = "/batch",
          consumes = MediaType.APPLICATION_JSON_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE)
  Response sendBatchJobResults(@RequestBody Object batchJobJsonFile);
}
