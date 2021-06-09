package com.zerohub.challenge.service;

import com.google.protobuf.Empty;
import com.zerohub.challenge.models.CurrencyRateException;
import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import com.zerohub.challenge.repository.CurrencyRateRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RateServiceImpl extends RatesServiceGrpc.RatesServiceImplBase {
  private final CurrencyRateRepository repository;

  public RateServiceImpl(final CurrencyRateRepository repository) {
    this.repository = repository;
  }

  @Override
  public void publish(final PublishRequest request, final StreamObserver<Empty> responseObserver) {
    try {
      repository.addCurrencyRate(request.getBaseCurrency(), request.getQuoteCurrency(), request.getPrice());
      responseObserver.onNext(Empty.newBuilder().build());
    } catch (CurrencyRateException e) {
      responseObserver.onError(new StatusRuntimeException(Status.CANCELLED));
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public void convert(final ConvertRequest request, final StreamObserver<ConvertResponse> responseObserver) {
    try {
      var rate = repository.getCurrencyRate(request.getFromCurrency(), request.getToCurrency());

      responseObserver.onNext(ConvertResponse.newBuilder().setPrice(
        (new BigDecimal(request.getFromAmount())).multiply(rate).setScale(4, RoundingMode.HALF_UP).toString()
      ).build());
    } catch (CurrencyRateException e) {
      responseObserver.onError(e);
    } finally {
      responseObserver.onCompleted();
    }
  }
}
