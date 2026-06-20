package com.watchandorder.component;

import com.watchandorder.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class Account {

    private final Properties properties;
    private int balance;

    public Account(Properties properties) {
        this.properties = properties;
        this.balance = properties.getInitialAccountBalance();
    }

    public boolean HasBalance() {
        return balance > 0;
    }

    synchronized
    public void updateBalanceAfterExecution(Order order) {
        // @TODO
        throw new RuntimeException("Not implemented yet");
    }

    synchronized
    public void reverseBalanceAfterCancel(Order order) {
        // @TODO
        throw new RuntimeException("Not implemented yet");
    }
}
