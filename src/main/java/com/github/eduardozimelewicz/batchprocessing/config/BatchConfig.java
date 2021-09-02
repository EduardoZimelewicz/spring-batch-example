package com.github.eduardozimelewicz.batchprocessing.config;

import com.github.eduardozimelewicz.batchprocessing.entity.Person;
import com.github.eduardozimelewicz.batchprocessing.mapper.PersonMapper;
import com.github.eduardozimelewicz.batchprocessing.processor.PersonItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  public DataSource dataSource;

  @Bean
  public JdbcPagingItemReader reader(PagingQueryProvider pagingQueryProvider) {
    return new JdbcPagingItemReaderBuilder<Person>()
            .name("personJdbcReader")
            .dataSource(dataSource)
            .queryProvider(pagingQueryProvider)
            .rowMapper(new PersonMapper())
            .pageSize(10)
            .build();
  }

  @Bean
  public SqlPagingQueryProviderFactoryBean queryProviderFactoryBean() {
    SqlPagingQueryProviderFactoryBean providerFactoryBean = new SqlPagingQueryProviderFactoryBean();

    providerFactoryBean.setDataSource(dataSource);
    providerFactoryBean.setSelectClause("select person_id, first_name, last_name");
    providerFactoryBean.setFromClause("from people");
    providerFactoryBean.setSortKey("person_id");

    return providerFactoryBean;
  }

  @Bean
  public PersonItemProcessor processor() {
    return new PersonItemProcessor();
  }

  @Bean
  public JsonFileItemWriter<Person> jsonFileItemWriter() {
    return new JsonFileItemWriterBuilder<Person>()
            .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
            .resource(new FileSystemResource("out/people.json"))
            .name("peopleJsonFileWriter")
            .build();
  }

  @Bean
  public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
    return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
  }

  @Bean
  public Step step1(JsonFileItemWriter<Person> writer, PersonItemProcessor processor) throws Exception {
    return stepBuilderFactory.get("step1")
            .<Person, Person> chunk(10)
            .reader(reader(queryProviderFactoryBean().getObject()))
            .processor(processor)
            .writer(jsonFileItemWriter())
            .build();
  }
}
