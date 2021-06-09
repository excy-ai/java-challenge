package com.zerohub.challenge.models;

public class CurrencyRateException extends Exception {
  public CurrencyRateException() {
    super();
  }

  public CurrencyRateException(String s) {
    super(s);
  }

  public CurrencyRateException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public CurrencyRateException(Throwable throwable) {
    super(throwable);
  }
}
