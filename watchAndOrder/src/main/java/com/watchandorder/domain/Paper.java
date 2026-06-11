package com.watchandorder.domain;

public record Paper(
        String name,
        int option_mode,
        int trade_calc_mode,
        int trade_mode,
        String path
) {
    @Override
    public String toString() {
        return "Paper name: " + name + ", option_mode: " + option_mode + ", trade_calc_mode: " + trade_calc_mode + ", trade_mode: " + trade_mode + ", path: " + path;
    }
};