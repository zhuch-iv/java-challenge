package com.zerohub.challenge.grpc;

import com.zerohub.challenge.application.exception.RateNotFoundException;
import com.zerohub.challenge.application.exception.SaveRateException;
import com.zerohub.challenge.application.exception.UnsupportedCurrencyException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import javax.validation.ConstraintViolationException;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(ConstraintViolationException.class)
    public StatusRuntimeException handleConstraintViolationException(ConstraintViolationException e) {
        Status status = Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        return status.asRuntimeException();
    }

    @GrpcExceptionHandler(SaveRateException.class)
    public StatusRuntimeException handleSaveRateException(SaveRateException e) {
        Status status = Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        return status.asRuntimeException();
    }

    @GrpcExceptionHandler(RateNotFoundException.class)
    public StatusRuntimeException handleRateNotFoundException(RateNotFoundException e) {
        Status status = Status.NOT_FOUND.withDescription(e.getMessage());
        return status.asRuntimeException();
    }

    @GrpcExceptionHandler(UnsupportedCurrencyException.class)
    public StatusRuntimeException handleUnsupportedCurrencyException(UnsupportedCurrencyException e) {
        Status status = Status.NOT_FOUND.withDescription(e.getMessage());
        return status.asRuntimeException();
    }
}
