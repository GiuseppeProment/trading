package com.watchandorder.component;

import com.watchandorder.domain.Candle;
import com.watchandorder.domain.Paper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/*
        == Watcher thread
        1. move paper to watching list
        2. get rates
        3. initialize ta4j
        4. for each strategy from list :
            apply strategy
                if signal
                    start Trader on paper
                    finalize thread.
        == Trader thread
        1. move paper to trading list
        2. if available Account.balance
                create Order according to Policy and put on bought list
                wait until Order is executed or canceled
                remove Order from bought list
                if Order executed put on executed list
                if Order canceled put on canceled list
           else
                create Order according to Policy and put on lost list
         3. move paper to traded list
*/
@Component
public class WatcherComponent {
    private static final Logger logger = LoggerFactory.getLogger(WatcherComponent.class);

    private final SharedStateComponent sharedState;
    private final Properties configurationProperties;
    private final RestClient restClient;

    public WatcherComponent(SharedStateComponent sharedState,
                            Properties configurationProperties) {
        this.sharedState = sharedState;
        this.configurationProperties = configurationProperties;
        this.restClient = RestClient.create(configurationProperties.getMetatraderUrl());
    }

    @Async("Executor")
    public void start(Paper paper) {
        logger.debug("Starting watcher for paper: {}", paper.name());
        sharedState.papers.remove(paper);
        sharedState.watching.add(paper);

        List<Candle> response = restClient
                .get().uri(String.format("copy_rates_from_pos/%s/%s/%s/%s",
                        paper.name(), "M1", 0, 20))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(new ParameterizedTypeReference<>() {});

            logger.info("Received candles for paper {}: {}", paper.name(), response.size());
            logger.info("First candle: {}", response.get(0).toString());

    }
}
