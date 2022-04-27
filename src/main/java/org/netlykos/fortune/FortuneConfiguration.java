package org.netlykos.fortune;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.codec.FortuneEncoder;
import org.netlykos.fortune.converter.FortuneHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.converter.HttpMessageConverter;

@Configuration
public class FortuneConfiguration {

  @Bean
  public HttpMessageConverter<Fortune> fortuneHttpMessageConverter() {
    return new FortuneHttpMessageConverter();
  }

  @Bean
  public HttpMessageWriter<Fortune> fortuneEncoderHttpMessageWriter() {
    return new EncoderHttpMessageWriter<>(new FortuneEncoder());
  }

}
