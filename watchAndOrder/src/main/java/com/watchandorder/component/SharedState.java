package com.watchandorder.component;

import com.watchandorder.domain.Order;
import com.watchandorder.domain.Paper;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SharedState {
    CopyOnWriteArrayList<Paper> papers = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> watching = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> trading = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> traded = new CopyOnWriteArrayList<>();

    CopyOnWriteArrayList<Order> pending = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> lost = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> executed = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> canceled = new CopyOnWriteArrayList<>();
}
