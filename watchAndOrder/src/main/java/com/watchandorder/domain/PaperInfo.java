package com.watchandorder.domain;

public record PaperInfo(int spread, int ask, int bid) {
    @Override
    public String toString() {
        return "PaperInfo spread: " + spread + ", ask: " + ask + ", bid: " + bid;
    }
}