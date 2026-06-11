package com.watchandorder.component;

import com.watchandorder.domain.Paper;
import com.watchandorder.domain.PaperInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);

    private final Properties configurationProperties;
    private final SharedStateComponent sharedState;
    private final WatcherComponent watcher;
    private final RestClient restClient;

    @Autowired
    public ApplicationRunner(Properties configurationProperties,
                             SharedStateComponent sharedState,
                             WatcherComponent watcher) {
        this.configurationProperties = configurationProperties;
        this.sharedState = sharedState;
        this.watcher = watcher;
        this.restClient = RestClient.create(configurationProperties.getMetatraderUrl());
    }


    @Override
    public void run(ApplicationArguments args) throws InterruptedException {

        logger.info("ApplicationRunner started.");
        do {
            List<Paper> response = restClient
                    .get().uri(String.format("symbols_get/%s", configurationProperties.getGroup()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().body(new ParameterizedTypeReference<>() {
                    });
            assert response != null;
            logger.info("Received papers: {}", response.size());
            removeNonRelevantStock(response);
            response.removeAll(sharedState.watching);
            logger.info("Papers after removing watching: {}", response.size());
            sharedState.papers.clear();
            sharedState.papers.addAll(response);
            logger.debug(response.stream().map(Paper::name).toList().toString());
            sharedState.papers.forEach(watcher::start);

            Thread.sleep(Duration.ofSeconds(2));

            logger.info("Paper list sizes:\npapers={}\nwatching={}\ntrading={}\ntraded={}",
                    sharedState.papers.size(),
                    sharedState.watching.size(),
                    sharedState.trading.size(),
                    sharedState.traded.size());

            logger.info("Order list sizes:\nbought={}\nlost={}\nexecuted={}",
                    sharedState.bought.size(),
                    sharedState.lost.size(),
                    sharedState.executed.size());

            Thread.sleep(Duration.ofSeconds(10));
        } while (false);
    }

    private void removeNonRelevantStock(List<Paper> response) {
        response.removeIf(paper -> !isStock(paper));
        logger.info("Filtered stock papers: {}", response.size());
        response.removeIf(paper -> !isLowSpread(paper));
        logger.info("Papers after removing Low spread stocks: {}", response.size());
    }

    private boolean isLowSpread(Paper paper) {
        PaperInfo paperInfo = restClient.get().uri("symbol_info/" + paper.name())
                .accept(MediaType.APPLICATION_JSON).retrieve().body(PaperInfo.class);
        logger.debug(ObjectUtils.nullSafeToString(paperInfo));
        if (paperInfo == null)
            return false;
        if (paperInfo.spread() <= configurationProperties.getMaxSpreadTick())
            return true;
        if (paperInfo.ask() > 0) {
            double spreadPercent = paperInfo.spread() * 0.01 / paperInfo.ask();
            return spreadPercent <= configurationProperties.getMaxSpreadPercent() / 100.0;
        }
        return false;
    }

    private boolean isStock(Paper paper) {
        logger.debug(paper.toString());
        return
                paper.option_mode() == 0 && // not an option
                        paper.trade_calc_mode() == 32 && // calc mode for stocks
                        paper.trade_mode() == 4 && // long and short allowed
                        paper.path().contains("BOVESPA") && // is a BOVESPA paper
                        (paper.name().endsWith("3") || // common stock
                                paper.name().endsWith("4") || // preferred stock
                                paper.name().endsWith("5")) &&  // other
                        (!paper.name().startsWith("TF") && !paper.name().startsWith("TAXA"));
    }
}