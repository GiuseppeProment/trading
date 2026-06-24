package com.watchandorder.domain;

import java.util.Objects;

public record Paper(String name, int option_mode, int trade_calc_mode, int trade_mode, String path) {

    @Override
    public String toString() {
        return "Paper name: " + name + ", option_mode: " + option_mode + ", trade_calc_mode: " + trade_calc_mode + ", trade_mode: " + trade_mode + ", path: " + path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Paper paper = (Paper) o;
        return Objects.equals(name, paper.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
};