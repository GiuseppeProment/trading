package com.trader.component;

import com.trader.domain.Order;
import com.trader.domain.Paper;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SharedState {
    CopyOnWriteArraySet<Paper> papers = new CopyOnWriteArraySet<>();
    CopyOnWriteArraySet<Paper> watching = new CopyOnWriteArraySet<>();
    CopyOnWriteArraySet<Paper> trading = new CopyOnWriteArraySet<>();
    CopyOnWriteArraySet<Paper> traded = new CopyOnWriteArraySet<>();

    CopyOnWriteArraySet<Paper> withoutRate = new CopyOnWriteArraySet<>();

    CopyOnWriteArrayList<Order> pending = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> lost = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> executed = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> canceled = new CopyOnWriteArrayList<>();
}
