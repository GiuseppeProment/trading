package com.trader.component;

import com.trader.domain.Paper;
import com.trader.domain.PaperInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;

@Component
public class TraderApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TraderApplicationRunner.class);

    private final Properties properties;
    private final SharedState sharedState;
    private final Watcher watcher;
    private final RestClient restClient;
    private final Helper helper;

    @Autowired
    public TraderApplicationRunner(Properties properties,
                                   SharedState sharedState,
                                   Watcher watcher, Helper helper) {
        this.properties = properties;
        this.sharedState = sharedState;
        this.watcher = watcher;
        this.helper = helper;
        this.restClient = RestClient.create(properties.getMetatraderUrl());
    }


    @Override
    public void run(ApplicationArguments args) throws InterruptedException {

        logger.info("ApplicationRunner started.");
        do {
            List<Paper> papers = getPapers();
            assert papers != null;
            logger.info("Received papers: {}", papers.size());
            removeNonRelevantStock(papers);
            // remove from watching paper not in papers list anymore
            sharedState.watching.removeIf(watchedPaper -> ! papers.contains(watchedPaper));
            // remove from papers list the ones already being watched
            papers.removeAll(sharedState.watching);
            logger.info("Papers after removing watching: {} {}...", papers.size(),
                    papers.stream().limit(properties.getMaxPaperOnInfoLogs()).map(Paper::name).toList());
            sharedState.papers.clear();
            sharedState.papers.addAll(papers);
            sharedState.papers.forEach(watcher::start);
            helper.sleep(Duration.ofSeconds(2));
            logger.info("Paper list sizes: papers={}, watching={}, trading={}, traded={}",
                    sharedState.papers.size(),
                    sharedState.watching.size(),
                    sharedState.trading.size(),
                    sharedState.traded.size());
            logger.info("Order list sizes: bought={}, lost={}, executed={}",
                    sharedState.pending.size(),
                    sharedState.lost.size(),
                    sharedState.executed.size());
            helper.sleep(Duration.ofMinutes(properties.getPapersRefreshRate()));
            logger.warn(String.format(
                    "[%s] papers that have been ignored from analysis due to rates not found:[%s]",
                    sharedState.withoutRate.size(),
                    sharedState.withoutRate.stream().limit(properties.getMaxPaperOnInfoLogs()).map(Paper::name).toList() ));
        } while (! properties.isSingleRun());

    }

    private List<Paper> getPapers() {
        return restClient
                .get().uri(String.format("symbols_get/%s", properties.getGroup()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    private PaperInfo getPaperInfo(Paper paper) {
        try {
            return
                    restClient.get().uri("symbol_info/" + paper.name())
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .body(PaperInfo.class);
        } catch (RestClientResponseException e) {
            logger.error(String.format("Exception reading symbol_info: %s. %s", paper.name(),e.getMessage()),e);
            return null;
        }
    }

    private void removeNonRelevantStock(List<Paper> papers) {
        papers.removeIf(paper -> !isStock(paper));
        logger.info("Filtered stock papers: {} {}...", papers.size(), papers.stream().limit(properties.getMaxPaperOnInfoLogs()).map(Paper::name).toList());
        papers.removeIf(paper -> !isLowSpread(paper));
        logger.info("Papers after removing Low spread stocks: {} {}...", papers.size(), papers.stream().limit(properties.getMaxPaperOnInfoLogs()).map(Paper::name).toList());
    }

    private boolean isLowSpread(Paper paper) {
        PaperInfo info = getPaperInfo(paper);
        if (info == null)
            return false;
        if (info.spread() <= properties.getMaxSpreadTick())
            return true;
        if (info.ask() > 0) {
            double spreadPercent = info.spread() * 0.01 / info.ask();
            return spreadPercent <= properties.getMaxSpreadPercent() / 100.0;
        }
        return true;
    }

    private boolean isStock(Paper paper) {
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