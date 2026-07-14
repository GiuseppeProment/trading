package com.trader.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record Candle(
        long time,
        double open,
        double high,
        double low,
        double close,
        double tick_volume,
        double spread,
        double real_volume
) {
    /**
     * Helper method para converter o timestamp Unix retornado pelo MT5
     * para o ZonedDateTime exigido pelo ta4j.
     */
    public ZonedDateTime getZonedDateTime() {
        return Instant.ofEpochSecond(this.time)
                .atZone(ZoneId.systemDefault()); // Altere para ZoneId.of("America/Sao_Paulo") se necessário
    }
}