package com.trader.component;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class Properties {
    private String metatraderUrl;
    private int maxSpreadTick;
    private double maxSpreadPercent;
    private String group;
    private int corePoolSize;
    private int maxPoolSize;
    private int papersRefreshRate;
    private boolean singleRun;
    private int orderCheckInterval;
    private int initialAccountBalance;
    private int maxPaperOnInfoLogs;

    public int getInitialAccountBalance() {
        return initialAccountBalance;
    }

    public void setInitialAccountBalance(int initialAccountBalance) {
        this.initialAccountBalance = initialAccountBalance;
    }

    public int getMaxSpreadTick() {
        return maxSpreadTick;
    }

    public void setMaxSpreadTick(int maxSpreadTick) {
        this.maxSpreadTick = maxSpreadTick;
    }

    public double getMaxSpreadPercent() {
        return maxSpreadPercent;
    }

    public void setMaxSpreadPercent(double maxSpreadPercent) {
        this.maxSpreadPercent = maxSpreadPercent;
    }

    public String getMetatraderUrl() {
        return metatraderUrl;
    }

    public void setMetatraderUrl(String metatraderUrl) {
        this.metatraderUrl = metatraderUrl;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getPapersRefreshRate() {
        return papersRefreshRate;
    }

    public void setPapersRefreshRate(int papersRefreshRate) {
        this.papersRefreshRate = papersRefreshRate;
    }

    public int getOrderCheckInterval() {
        return orderCheckInterval;
    }

    public void setOrderCheckInterval(int orderCheckInterval) {
        this.orderCheckInterval = orderCheckInterval;
    }

    public boolean isSingleRun() {
        return singleRun;
    }

    public void setSingleRun(boolean singleRun) {
        this.singleRun = singleRun;
    }

    public int getMaxPaperOnInfoLogs() {
        return maxPaperOnInfoLogs;
    }

    public void setMaxPaperOnInfoLogs(int maxPaperOnInfoLogs) {
        this.maxPaperOnInfoLogs = maxPaperOnInfoLogs;
    }
}


