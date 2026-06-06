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
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

@Component
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);

    private final Executor executor;
    private final Properties configurationProperties;
    private final SharedStateComponent sharedStateComponent;
    private final RestClient restClient;

    @Autowired
    public ApplicationRunner(Executor executor,
                             Properties configurationProperties,
                             SharedStateComponent sharedStateComponent) {
        this.executor = executor;
        this.configurationProperties = configurationProperties;
        this.sharedStateComponent = sharedStateComponent;
        this.restClient = RestClient.create(configurationProperties.getMetatraderUrl());
    }


    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        logger.info("ApplicationRunner started.");

        do {
            List<Paper> response = restClient
                    .get()
                    .uri("symbols_get/group=\"!*F,!TF*,!TAXA*,*3,*4,*5\"")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            assert response != null;
            logger.info("Received papers: {}", response.size());
            response.removeIf(paper -> !isStock(paper));
            logger.info("Filtered stock papers: {}", response.size());
            response.removeIf(paper -> !isLowSpread(paper));
            logger.info("Low spread stock papers: {}", response.size());
            sharedStateComponent.papers.clear();
            sharedStateComponent.papers.addAll(response);
            Thread.sleep(Duration.ofSeconds(5));
        } while (true);

        /*
        == ApplicationRunner thread (main) ==
        1. fill select list with relevant papers, can use Group to filter by pattern name
        2. start a Watcher on each select
        3. wait a second and go to step 1
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
    }

    private boolean isLowSpread(Paper paper) {
        PaperInfo paperInfo = restClient
                .get()
                .uri("symbol_info/" + paper.name())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PaperInfo.class);
        if (paperInfo == null) return false;
        if (paperInfo.spread() <= configurationProperties.getMaxSpreadTick()) return true;
        if (paperInfo.ask() > 0 ) {
            double spreadPercent = paperInfo.spread()*0.01 / paperInfo.ask();
            return spreadPercent <= configurationProperties.getMaxSpreadPercent()/100.0;
        }
        return false;
    }

    private boolean isStock(Paper paper) {
        return paper.option_mode() == 0 &&
                paper.trade_calc_mode() == 32 &&
                paper.trade_mode() == 4 &&
                paper.path().contains("BOVESPA") &&
                ( paper.name().endsWith("3") || paper.name().endsWith("4") || paper.name().endsWith("5") ) &&
                ( ! paper.name().startsWith("TF") && ! paper.name().startsWith("TAXA") )
                ;
    }
}
