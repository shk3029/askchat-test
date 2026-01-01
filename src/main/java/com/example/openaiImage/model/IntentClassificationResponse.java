package com.example.openaiImage.model;

public class IntentClassificationResponse {
    private Intent intent;
    private String userMessage;
    private String response;
    private double confidence;

    public IntentClassificationResponse() {
    }

    public IntentClassificationResponse(Intent intent, String userMessage, String response, double confidence) {
        this.intent = intent;
        this.userMessage = userMessage;
        this.response = response;
        this.confidence = confidence;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
