package com.example.openaiImage.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingProgressService {

    private final AtomicInteger currentCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);
    private final AtomicBoolean isIndexing = new AtomicBoolean(false);
    private volatile String currentMessage = "";

    public void startIndexing(int total) {
        currentCount.set(0);
        totalCount.set(total);
        isIndexing.set(true);
        currentMessage = "색인 시작...";
    }

    public void updateProgress(int current, String message) {
        currentCount.set(current);
        currentMessage = message;
    }

    public void finishIndexing() {
        isIndexing.set(false);
        currentMessage = "색인 완료!";
    }

    public boolean isIndexing() {
        return isIndexing.get();
    }

    public int getCurrentCount() {
        return currentCount.get();
    }

    public int getTotalCount() {
        return totalCount.get();
    }

    public String getCurrentMessage() {
        return currentMessage;
    }
}
