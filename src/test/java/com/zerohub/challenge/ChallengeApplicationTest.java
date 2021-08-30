package com.zerohub.challenge;

import com.zerohub.challenge.application.RateRepository;
import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class ChallengeApplicationTest {
    private static final String BTC = "BTC";
    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String UAH = "UAH";
    private static final String RUB = "RUB";
    private static final String LTC = "LTC";
    private static final String AUD = "AUD";

    @GrpcClient("ratesService")
    private RatesServiceGrpc.RatesServiceBlockingStub ratesService;

    @Autowired
    private RateRepository graph;

    @AfterEach
    private void shutDownTest() {
        graph.removeAll();
    }

    private void setupConvertTest() {
        setupTest(List.of(
            toPublishRequest(new String[]{BTC, EUR, "50000.0000"}),
            toPublishRequest(new String[]{EUR, USD, "1.2000"}),
            toPublishRequest(new String[]{EUR, AUD, "1.5000"}),
            toPublishRequest(new String[]{USD, RUB, "80.0000"}),
            toPublishRequest(new String[]{UAH, RUB, "4.0000"}),
            toPublishRequest(new String[]{LTC, BTC, "0.0400"})
//            toPublishRequest(new String[]{LTC, USD, "2320.0000"})
        ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("convertTestData")
    void convertTest(String ignore, ConvertRequest request, String expectedPrice) {
        setupConvertTest();

        ConvertResponse response = ratesService.convert(request);

        assertEquals(expectedPrice, response.getPrice());
    }

    private static Stream<Arguments> convertTestData() {
        return Stream.of(
            Arguments.of("Same currency", toConvertRequest(new String[]{BTC, BTC, "0.9997"}), "0.9997"),
            Arguments.of("Simple conversion", toConvertRequest(new String[]{EUR, BTC, "50000.0000"}), "1.0000"),
            Arguments.of("Reversed conversion", toConvertRequest(new String[]{BTC, EUR, "1.0000"}), "50000.0000"),
            Arguments.of("Convert with one hop", toConvertRequest(new String[]{BTC, AUD, "1.0000"}), "75000.0000"),
            Arguments.of("Convert with two hops", toConvertRequest(new String[]{BTC, RUB, "1.0000"}), "4800000.0000"),
            Arguments.of("Reversed conversion with two hops", toConvertRequest(new String[]{RUB, EUR, "96.0000"}), "1.0000")
        );
    }

    private void setupFindMinimalRateTest() {
        setupTest(List.of(
            toPublishRequest(new String[]{BTC, USD, "60000.0000"}),
            toPublishRequest(new String[]{BTC, LTC, "58.0000"}),
            toPublishRequest(new String[]{LTC, USD, "1000.0000"})
        ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("findMinimalTestData")
    void findMinimalTest(String ignore, ConvertRequest request, String expectedPrice) {
        setupFindMinimalRateTest();

        ConvertResponse response = ratesService.convert(request);

        assertEquals(expectedPrice, response.getPrice());
    }

    private static Stream<Arguments> findMinimalTestData() {
        return Stream.of(
            Arguments.of("Find minimal with two hops", toConvertRequest(new String[]{BTC, USD, "1"}), "58000.0000")
        );
    }

    private void setupTest(List<PublishRequest> rates) {
        rates.forEach(rate -> ratesService.publish(rate));
    }

    public static PublishRequest toPublishRequest(String[] args) {
        return PublishRequest
            .newBuilder()
            .setBaseCurrency(args[0])
            .setQuoteCurrency(args[1])
            .setPrice(args[2])
            .build();
    }

    public static ConvertRequest toConvertRequest(String[] args) {
        return ConvertRequest
            .newBuilder()
            .setFromCurrency(args[0])
            .setToCurrency(args[1])
            .setFromAmount(args[2])
            .build();
    }
}
