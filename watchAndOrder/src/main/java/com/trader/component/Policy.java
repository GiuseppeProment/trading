package com.trader.component;

import com.trader.domain.Order;
import com.trader.domain.Paper;
import org.springframework.stereotype.Component;

@Component
public class Policy {
    private final Properties properties;
    private final Account account;

    public Policy(Properties properties, Account account) {
        this.properties = properties;
        this.account = account;
    }

    synchronized
    public Order createOrder(Paper paper, Strategy strategy) {
        // @TODO implement order creation logic according to strategy and account balance
        // get last tick for paper
        // calculate lot size according to strategy and account balance
        // verify order state according to balance : pending (possible) or lost (impossible due to insufficient balance)
        // create Order with calculated lot size and strategy signal (buy/sell)
        // debit balance according to order state and lot size
        return null;
    }
}
