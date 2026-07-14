package com.trader.service;

import com.trader.domain.Candle;
import org.ta4j.core.BarSeries;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class MarketDataService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    // O ObjectMapper nativo já resolve tudo automaticamente se as chaves forem iguais
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BarSeries fetchAndParseMarketData(String symbol) throws Exception {
        // 1. Faz a requisição HTTP para a API Python (Abordagem 2)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/candles?symbol=" + symbol))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 2. O Jackson faz o mapeamento direto das chaves do objeto JSON para a lista de Records
        List<Candle> candles = objectMapper.readValue(
                response.body(),
                new TypeReference<List<Candle>>() {}
        );

        /*
        // 3. Inicializa a série temporal do ta4j
        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol)
                .withNumTypeOf(DecimalNum::valueOf)
                .build();

        // 4. Alimenta o ta4j usando o helper de conversão de tempo
        for (Mt5Candle candle : candles) {
            series.addBar(
                    candle.getZonedDateTime(),
                    candle.open(),
                    candle.high(),
                    candle.low(),
                    candle.close(),
                    candle.volume()
            );
        }
        return series;
        */
        return null;
    }
}