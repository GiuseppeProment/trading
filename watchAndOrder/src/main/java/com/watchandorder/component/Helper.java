package com.watchandorder.component;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class Helper {

    public void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}