package com.zerohub.challenge.application;

import com.zerohub.challenge.currency.Rate;

import java.util.Optional;

public interface RateRepository {

    boolean containsCurrency(String currency);

    Optional<Rate> getRate(String from, String to);

    void putRate(Rate rate);

    void removeAll();
}
