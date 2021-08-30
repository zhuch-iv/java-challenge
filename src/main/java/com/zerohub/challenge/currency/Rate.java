package com.zerohub.challenge.currency;

import lombok.Value;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Value
public class Rate {
    public static final int PRINT_SCALE = 4;
    public static final int SCALE = 12;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @NotBlank
    String base;
    @NotBlank
    String quote;
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal value;

    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(this.value).setScale(PRINT_SCALE, ROUNDING_MODE);
    }

    public Rate reverse() {
        return new Rate(quote, base, BigDecimal.ONE.divide(this.value, 12, ROUNDING_MODE));
    }

    public Rate exchange(Rate rate) {
        return new Rate(base, rate.quote, rate.value.multiply(this.value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rate rate = (Rate) o;

        if (!Objects.equals(base, rate.base)) return false;
        if (!Objects.equals(quote, rate.quote)) return false;
        return value != null ? value.compareTo(rate.value) == 0 : rate.value == null;
    }

    @Override
    public int hashCode() {
        int result = base != null ? base.hashCode() : 0;
        result = 31 * result + (quote != null ? quote.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
