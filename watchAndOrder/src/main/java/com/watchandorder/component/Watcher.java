package com.watchandorder.component;

import com.watchandorder.domain.Candle;
import com.watchandorder.domain.Paper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
public class Watcher {
    private static final Logger logger = LoggerFactory.getLogger(Watcher.class);

    private final SharedState sharedState;
    private final Properties properties;
    private final RestClient restClient;
    private final List<Strategy> strategies;
    private final Treader treader;
    private final Helper helper;

    public Watcher(SharedState sharedState,
                   Properties properties,
                   List<Strategy> strategies,
                   Treader treader,
                   Helper helper) {
        this.sharedState = sharedState;
        this.properties = properties;
        this.strategies = strategies;
        this.treader = treader;
        this.helper = helper;
        this.restClient = RestClient.create(properties.getMetatraderUrl());
    }

    @Async("Executor")
    public void start(Paper paper) {
        logger.debug("Starting Watcher for paper: {}", paper.getName());
        sharedState.papers.remove(paper);
        sharedState.watching.add(paper);
        do {
            List<Candle> candles_M1 = restClient
                    .get().uri(String.format("copy_rates_from_pos/%s/%s/%s/%s",
                            paper.getName(),
                            /*timeframe*/"M1",
                            /*start*/0,
                            /*count*/20))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().body(new ParameterizedTypeReference<>() {});

            logger.info("Received candles M1 for paper {}: {}", paper.getName(), candles_M1.size());
            logger.debug("First candle: {}", candles_M1.getFirst().toString());

            strategies.forEach(strategy -> {
                if (strategy.hasSignalEntryFor(candles_M1)) {
                    treader.start(paper,strategy);
                }
            });
            helper.sleep(Duration.ofMinutes(1));
        } while (sharedState.watching.contains(paper));
    }

}
