package com.zerohub.challenge.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@AllArgsConstructor
@Builder
public class CurrencyPair {

    @NonNull
    private String base;

    @NonNull
    private String pair;

    @Override
    public String toString() {
        return base + "/" + pair;
    }

    private static final Pattern PAIR_PATTERN = Pattern.compile("([A-Z]{3})/([A-Z]{3})");

    public static CurrencyPair of(final String pairString) {
        Matcher m = PAIR_PATTERN.matcher(pairString);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid currency pair format: " + pairString);
        }
        return new CurrencyPair(m.group(1), m.group(2));
    }

}
