package com.zerohub.challenge.configuration;

import com.zerohub.challenge.repository.CurrencyRateRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurrencyRateConfiguration {

  @Bean
  public CurrencyRateRepository currencyRateRepository() {
    return new CurrencyRateRepository();
  }
}
