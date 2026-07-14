package com.trader.component;

import com.trader.domain.Candle;

import java.util.List;

public interface Strategy {
    boolean hasSignalEntryFor(List<Candle> response);
    String toString();
}
