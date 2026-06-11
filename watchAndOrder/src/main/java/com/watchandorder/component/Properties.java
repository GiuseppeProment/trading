package com.watchandorder.component;

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
}


