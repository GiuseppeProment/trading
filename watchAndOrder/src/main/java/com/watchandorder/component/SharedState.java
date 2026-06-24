package com.watchandorder.component;

import com.watchandorder.domain.Order;
import com.watchandorder.domain.Paper;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SharedState {
    CopyOnWriteArraySet<Paper> papers = new CopyOnWriteArraySet<>();
    CopyOnWriteArraySet<Paper> watching = new CopyOnWriteArraySet<>();
    CopyOnWriteArraySet<Paper> trading = new CopyOnWriteArraySet<>();
    CopyOnWriteArraySet<Paper> traded = new CopyOnWriteArraySet<>();

    CopyOnWriteArraySet<Paper> whithoutRate = new CopyOnWriteArraySet<>();

    CopyOnWriteArrayList<Order> pending = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> lost = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> executed = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> canceled = new CopyOnWriteArrayList<>();
}
