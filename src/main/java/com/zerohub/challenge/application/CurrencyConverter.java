package com.zerohub.challenge.application;

import com.zerohub.challenge.application.exception.RateNotFoundException;
import com.zerohub.challenge.application.exception.SaveRateException;
import com.zerohub.challenge.application.exception.UnsupportedCurrencyException;
import com.zerohub.challenge.currency.Rate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Service
@Validated
public class CurrencyConverter {

    private final RateRepository repository;

    @Autowired
    public CurrencyConverter(RateRepository repository) {
        this.repository = repository;
    }

    public void saveRate(@Valid Rate rate) {
        if (rate.getBase().equals(rate.getQuote())) {
            throw new SaveRateException("Same currency");
        }
        repository.putRate(rate);
    }

    public BigDecimal convert(
        @NotBlank String fromCurrency,
        @NotBlank String toCurrency,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
    ) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        } else if (!repository.containsCurrency(fromCurrency)) {
            throw new UnsupportedCurrencyException("Unsupported currency - " + fromCurrency);
        } else if (!repository.containsCurrency(toCurrency)) {
            throw new UnsupportedCurrencyException("Unsupported currency - " + toCurrency);
        }

        Rate rate = repository.getRate(fromCurrency, toCurrency)
            .orElseThrow(() -> {
                return new RateNotFoundException(
                    "Convert rate not found - from currency: " + fromCurrency + ", to currency: " + toCurrency
                );
            });
        return rate.convert(amount);
    }
}
