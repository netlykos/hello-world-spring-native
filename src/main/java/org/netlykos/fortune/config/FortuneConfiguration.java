package org.netlykos.fortune.config;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.codec.FortuneCategoryEncoder;
import org.netlykos.fortune.codec.FortuneEncoder;
import org.netlykos.fortune.converter.FortuneHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class FortuneConfiguration {

  @Bean
  HttpMessageConverter<Fortune> fortuneHttpMessageConverter() {
    return new FortuneHttpMessageConverter();
  }

  @Bean
  WebFluxConfigurer webFluxConfigurer() {
    return new WebFluxConfigurer() {
      @Override
      public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new FortuneEncoder());
        configurer.customCodecs().register(new FortuneCategoryEncoder());
      }
    };
  }

}
