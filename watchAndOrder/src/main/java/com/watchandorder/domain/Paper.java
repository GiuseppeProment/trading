package com.watchandorder.domain;

public record Paper(
        String name,
        int option_mode,
        int trade_calc_mode,
        int trade_mode,
        String path
) {};