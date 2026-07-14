package com.trader.domain;

import java.util.Objects;

public final class Order {
    private State state;

    public Order(State state) {
        this.state = state;
    }

    public static enum State {PENDING, LOST, EXECUTED, CANCELED}

    @Override
    public String toString() {
        return "Order{" +
                "state=" + state +
                '}';
    }

    public State state() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Order) obj;
        return Objects.equals(this.state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }


}
