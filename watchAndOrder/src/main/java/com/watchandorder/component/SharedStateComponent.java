package com.watchandorder.component;

import com.watchandorder.domain.Order;
import com.watchandorder.domain.Paper;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SharedStateComponent {
    CopyOnWriteArrayList<Paper> papers = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> select = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> watching = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> trading = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Paper> traded = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> bought = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> lost = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Order> executed = new CopyOnWriteArrayList<>();
}
