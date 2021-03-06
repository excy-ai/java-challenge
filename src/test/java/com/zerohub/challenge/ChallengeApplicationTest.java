package com.zerohub.challenge;

import com.google.protobuf.Empty;
import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import com.zerohub.challenge.repository.CurrencyRateRepository;
import com.zerohub.challenge.service.RateServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.testing.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@Slf4j
public class ChallengeApplicationTest {
  private static final String BTC = "BTC";
  private static final String EUR = "EUR";
  private static final String USD = "USD";
  private static final String UAH = "UAH";
  private static final String RUB = "RUB";
  private static final String LTC = "LTC";
  private static final String JOD = "JOD";
  private static final String GBP = "GBP";
  private RateServiceImpl rateService;

  @BeforeEach
  public void setup() {
    rateService = new RateServiceImpl(new CurrencyRateRepository());
    var rates = List.of(
      toPublishRequest(new String[]{BTC, EUR, "50000.0000"}),
      toPublishRequest(new String[]{EUR, USD, "1.2000"}),
      toPublishRequest(new String[]{USD, RUB, "80.0000"}),
      toPublishRequest(new String[]{UAH, RUB, "4.0000"}),
      toPublishRequest(new String[]{LTC, BTC, "0.0400"}),
      toPublishRequest(new String[]{LTC, USD, "2320.0000"}),
      toPublishRequest(new String[]{JOD, GBP, "1.0000"})
    );
    StreamRecorder<Empty> responseObserver = StreamRecorder.create();
    for (var rate : rates) {
      rateService.publish(rate, responseObserver);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testData")
  void ConvertTest(String ignore, ConvertRequest request, BigDecimal expectedPrice) {
    StreamRecorder<ConvertResponse> responseObserver = StreamRecorder.create();
    rateService.convert(request, responseObserver);
    String actual = responseObserver.getValues().get(0).getPrice();
    assertEquals(expectedPrice, new BigDecimal(actual));
  }

  private static Stream<Arguments> testData() {

    return Stream.of(
      Arguments.of("Same currency", toConvertRequest(new String[]{BTC, BTC, "0.9997"}), "0.9997"),
      Arguments.of("Simple conversion", toConvertRequest(new String[]{EUR, BTC, "50000.0000"}), "1.0000"),
      Arguments.of("Reversed conversion", toConvertRequest(new String[]{BTC, EUR, "1.0000"}), "50000.0000"),
      Arguments.of("Convert with one hop", toConvertRequest(new String[]{BTC, USD, "1.0000"}), "58000.0000"),
      Arguments.of("Convert with two hops", toConvertRequest(new String[]{BTC, RUB, "1.0000"}), "4640000.0000"),
      Arguments.of("Reversed conversion with two hops", toConvertRequest(new String[]{RUB, EUR, "96.0000"}), "1.0000")
    );
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("notFoundTestData")
  void NotFoundTest(String ignore, ConvertRequest request) {
    StreamRecorder<ConvertResponse> responseObserver = StreamRecorder.create();
    rateService.convert(request, responseObserver);
    assertNotNull(responseObserver.getError());
  }

  private static Stream<Arguments> notFoundTestData() {

    return Stream.of(
      Arguments.of("Not found BTC/JOD", toConvertRequest(new String[]{BTC, JOD, "1"})),
      Arguments.of("Not found RUB/JOD", toConvertRequest(new String[]{RUB, JOD, "1"})),
      Arguments.of("Not found USD/JOD", toConvertRequest(new String[]{USD, JOD, "1"}))
    );
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("cantPublishData")
  void CantPublishTest(String ignore, PublishRequest request) {
    StreamRecorder<Empty> responseObserver = StreamRecorder.create();
    rateService.publish(request, responseObserver);
    assertNotNull(responseObserver.getError());
  }

  private static Stream<Arguments> cantPublishData() {

    return Stream.of(
      Arguments.of("Can't publish JOD/JOD ", toPublishRequest(new String[]{JOD, JOD, "1"})),
      Arguments.of("Can't publish RUB/RUB ", toPublishRequest(new String[]{RUB, RUB, "1"}))
    );
  }

  private static PublishRequest toPublishRequest(String[] args) {
    return PublishRequest
      .newBuilder()
      .setBaseCurrency(args[0])
      .setQuoteCurrency(args[1])
      .setPrice(args[2])
      .build();
  }

  private static ConvertRequest toConvertRequest(String[] args) {
    return ConvertRequest
      .newBuilder()
      .setFromCurrency(args[0])
      .setToCurrency(args[1])
      .setFromAmount(args[2])
      .build();
  }
}
