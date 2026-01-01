package com.example.openaiImage.controller;

import com.example.openaiImage.model.IntentClassificationResponse;
import com.example.openaiImage.service.ChatService;
import com.example.openaiImage.service.MasterAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskAIController {

    private final ChatService chatService;
    private final MasterAgentService masterAgentService;

    public AskAIController(ChatService chatService, MasterAgentService masterAgentService) {
        this.chatService = chatService;
        this.masterAgentService = masterAgentService;
    }

    @GetMapping("/ask")
    public  String getResponse(@RequestParam("message") String message){
           return chatService.getResponse(message);
    }

    @GetMapping("/ask-ai")
    public  String getResponseOptions(@RequestParam("message") String message){
        return chatService.getResponseOptions(message);
    }

    @GetMapping("/master-agent")
    public IntentClassificationResponse getMasterAgentResponse(@RequestParam("message") String message){
        return masterAgentService.handleUserQuery(message);
    }
}
