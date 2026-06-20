package com.watchandorder.component;

import com.watchandorder.domain.Candle;

import java.util.List;

public interface Strategy {
    boolean hasSignalEntryFor(List<Candle> response);
    String toString();
}
