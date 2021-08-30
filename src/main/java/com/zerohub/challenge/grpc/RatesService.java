package com.zerohub.challenge.grpc;

import com.google.protobuf.Empty;
import com.zerohub.challenge.application.CurrencyConverter;
import com.zerohub.challenge.currency.Rate;
import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Slf4j
@GrpcService
public class RatesService extends RatesServiceGrpc.RatesServiceImplBase {

    private final CurrencyConverter converter;

    @Autowired
    public RatesService(CurrencyConverter converter) {
        this.converter = converter;
    }


    @Override
    public void publish(PublishRequest request, StreamObserver<Empty> responseObserver) {
        converter.saveRate(new Rate(request.getBaseCurrency(), request.getQuoteCurrency(), new BigDecimal(request.getPrice())));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void convert(ConvertRequest request, StreamObserver<ConvertResponse> responseObserver) {
        BigDecimal amount = converter.convert(
            request.getFromCurrency(), request.getToCurrency(), new BigDecimal(request.getFromAmount())
        );

        ConvertResponse response = ConvertResponse.newBuilder()
            .setPrice(amount.toString())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
