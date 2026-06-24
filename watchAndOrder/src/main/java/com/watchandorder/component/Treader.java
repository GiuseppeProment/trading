package com.watchandorder.component;

import com.watchandorder.domain.Order;
import com.watchandorder.domain.Paper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class Treader {
    private static final Logger logger = LoggerFactory.getLogger(Treader.class);
    private final SharedState sharedState;
    private final Properties properties;
    private final RestClient restClient;
    private final Helper helper;
    private final Account account;
    private final Policy policy;

    public Treader(SharedState sharedState,
                   Properties properties,
                   Helper helper,
                   Account account,
                   Policy policy) {
        this.sharedState = sharedState;
        this.properties = properties;
        this.helper = helper;
        this.account = account;
        this.policy = policy;
        this.restClient = RestClient.create(properties.getMetatraderUrl());
    }

    @Async("Executor")
    public void start(Paper paper, Strategy strategy) {
        logger.info("Starting Treader for paper: {}, strategy: {}", paper.name(), strategy.toString());
        sharedState.watching.remove(paper);
        sharedState.trading.add(paper);
        try {
            Order order = policy.createOrder(paper, strategy);
            if (order.state() ==  Order.State.PENDING) {
                sharedState.pending.add(order);
                sendAndWaitExecution(order);
                sharedState.pending.remove(order);
                sharedState.trading.remove(paper);
                sharedState.traded.add(paper);
                if (order.state() == Order.State.EXECUTED) {
                    sharedState.executed.add(order);
                    account.updateBalanceAfterExecution(order);
                } else if (order.state() == Order.State.CANCELED) {
                    sharedState.canceled.add(order);
                    account.reverseBalanceAfterCancel(order);
                }
            } else if (order.state() == Order.State.LOST) {
                sharedState.lost.add(order);
            } else {
                logger.error("Unexpected order state: {}", order);
            }
        } finally {
            sharedState.trading.remove(paper);
        }
    }

    private void sendAndWaitExecution(Order order) {
        sendToMetatrader(order);
        do {
            helper.sleep(Duration.ofSeconds(properties.getOrderCheckInterval()));
            updateState(order);
        } while (order.state() == Order.State.PENDING);
    }

    private void updateState(Order order) {
        // @TODO implement check order state from metatrader and update order state accordingly
        throw  new RuntimeException("Not implemented yet");
    }

    private void sendToMetatrader(Order order) {
        // @TODO implement send order to metatrader
        throw  new RuntimeException("Not implemented yet");
    }
}
