package com.watchandorder.domain;

public class Paper {
    private final String name;
    private final int option_mode;
    private final int trade_calc_mode;
    private final int trade_mode;
    private final String path;
    private PaperInfo paperInfo;

    public Paper(String name, int option_mode, int trade_calc_mode, int trade_mode, String path) {
        this.name = name;
        this.option_mode = option_mode;
        this.trade_calc_mode = trade_calc_mode;
        this.trade_mode = trade_mode;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public int getOption_mode() {
        return option_mode;
    }

    public int getTrade_calc_mode() {
        return trade_calc_mode;
    }

    public int getTrade_mode() {
        return trade_mode;
    }

    public String getPath() {
        return path;
    }

    public PaperInfo getPaperInfo() {
        return paperInfo;
    }

    public void setPaperInfo(PaperInfo paperInfo) {
        this.paperInfo = paperInfo;
    }

    @Override
    public String toString() {
        return "Paper name: " + name + ", option_mode: " + option_mode + ", trade_calc_mode: " + trade_calc_mode + ", trade_mode: " + trade_mode + ", path: " + path;
    }
};