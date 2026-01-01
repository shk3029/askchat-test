package com.example.openaiImage.model;

import java.util.List;

public class IntentDocument {
    private String id;
    private Intent intent;
    private String query;
    private List<String> keywords;
    private List<Double> embedding;

    public IntentDocument() {
    }

    public IntentDocument(String id, Intent intent, String query, List<String> keywords, List<Double> embedding) {
        this.id = id;
        this.intent = intent;
        this.query = query;
        this.keywords = keywords;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<Double> embedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}
