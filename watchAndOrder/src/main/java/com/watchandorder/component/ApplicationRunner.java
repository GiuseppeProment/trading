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

    private final Properties properties;
    private final SharedState sharedState;
    private final Watcher watcher;
    private final RestClient restClient;
    private final Helper helper;

    @Autowired
    public ApplicationRunner(Properties properties,
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
            papers.forEach(this::GetAndAddInfo);
            removeNonRelevantStock(papers);
            papers.removeAll(sharedState.watching);
            logger.info("Papers after removing watching: {}", papers.size());
            sharedState.papers.clear();
            sharedState.papers.addAll(papers);
            logger.debug(papers.stream().map(Paper::getName).toList().toString());
            sharedState.papers.forEach(watcher::start);
            helper.sleep(Duration.ofSeconds(2));
            logger.info("""
                   ======================
                   Paper list sizes:
                   ======================
                   papers={}
                   watching={}
                   trading={}
                   traded={}
                   """,
                    sharedState.papers.size(),
                    sharedState.watching.size(),
                    sharedState.trading.size(),
                    sharedState.traded.size());
            logger.info("""
                    =================
                    Order list sizes:
                    =================
                    bought={}
                    lost={}
                    executed={}
                    """,
                    sharedState.pending.size(),
                    sharedState.lost.size(),
                    sharedState.executed.size());
            if (properties.isSingleRun()) {
                logger.info("Single run mode enabled. Exiting after one iteration.");
                break;
            }
            helper.sleep(Duration.ofSeconds(properties.getPapersRefreshRate()));
        } while (true);
    }

    private List<Paper> getPapers() {
        return restClient
                .get().uri(String.format("symbols_get/%s", properties.getGroup()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(new ParameterizedTypeReference<>() {});
    }

    private void GetAndAddInfo(Paper paper) {
        PaperInfo paperInfo = restClient.get().uri("symbol_info/" + paper.getName())
                .accept(MediaType.APPLICATION_JSON).retrieve().body(PaperInfo.class);
        logger.debug(ObjectUtils.nullSafeToString(paperInfo));
        paper.setPaperInfo(paperInfo);
    }

    private void removeNonRelevantStock(List<Paper> response) {
        response.removeIf(paper -> !isStock(paper));
        logger.info("Filtered stock papers: {}", response.size());
        response.removeIf(paper -> !isLowSpread(paper));
        logger.info("Papers after removing Low spread stocks: {}", response.size());
    }

    private boolean isLowSpread(Paper paper) {
        if (paper.getPaperInfo() == null)
            return false;
        if (paper.getPaperInfo().spread() <= properties.getMaxSpreadTick())
            return true;
        if (paper.getPaperInfo().ask() > 0) {
            double spreadPercent = paper.getPaperInfo().spread() * 0.01 / paper.getPaperInfo().ask();
            return spreadPercent <= properties.getMaxSpreadPercent() / 100.0;
        }
        return false;
    }

    private boolean isStock(Paper paper) {
        logger.debug(paper.toString());
        return
                paper.getOption_mode() == 0 && // not an option
                        paper.getTrade_calc_mode() == 32 && // calc mode for stocks
                        paper.getTrade_mode() == 4 && // long and short allowed
                        paper.getPath().contains("BOVESPA") && // is a BOVESPA paper
                        (paper.getName().endsWith("3") || // common stock
                                paper.getName().endsWith("4") || // preferred stock
                                paper.getName().endsWith("5")) &&  // other
                        (!paper.getName().startsWith("TF") && !paper.getName().startsWith("TAXA"));
    }
}