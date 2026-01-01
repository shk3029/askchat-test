package com.example.openaiImage.service;

import com.example.openaiImage.model.Intent;
import com.example.openaiImage.model.IntentClassificationResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class MasterAgentService {

    private final IntentClassifierService intentClassifierService;
    private final ChatModel chatModel;

    public MasterAgentService(IntentClassifierService intentClassifierService, ChatModel chatModel) {
        this.intentClassifierService = intentClassifierService;
        this.chatModel = chatModel;
    }

    public IntentClassificationResponse handleUserQuery(String userMessage) {
        Intent classifiedIntent = intentClassifierService.classifyIntent(userMessage);

        String response = String.format("현재 질문은 '%s' 인텐트입니다.", classifiedIntent.getKoreanName());

        double confidence = calculateConfidence(classifiedIntent);

        return new IntentClassificationResponse(classifiedIntent, userMessage, response, confidence);
    }

    private String generateResponseForIntent(Intent intent, String userMessage) {
        String systemPrompt = buildSystemPromptForIntent(intent);

        String fullPrompt = systemPrompt + "\n\n사용자 질문: " + userMessage;

        return chatModel.call(
                new Prompt(
                        fullPrompt,
                        OpenAiChatOptions.builder()
                                .withModel("gpt-4o")
                                .withTemperature(0.7)
                                .build()
                )).getResult().getOutput().getContent();
    }

    private String buildSystemPromptForIntent(Intent intent) {
        return switch (intent) {
            case CARD_RECOMMENDATION -> """
                    당신은 전문 카드 추천 상담사입니다.
                    사용자의 소비 패턴과 요구사항을 분석하여 최적의 카드를 추천해주세요.
                    주요 혜택, 연회비, 추천 이유를 포함하여 상세히 안내해주세요.
                    """;
            case CARD_GUIDE -> """
                    당신은 카드 상품 안내 전문가입니다.
                    카드의 상세 정보, 혜택, 사용 조건, 연회비 등을 명확하게 안내해주세요.
                    사용자가 궁금해하는 카드 정보를 정확하게 제공해주세요.
                    """;
            case EVENT_INFO -> """
                    당신은 이벤트 안내 담당자입니다.
                    진행 중인 프로모션, 이벤트, 특별 행사에 대해 상세히 안내해주세요.
                    참여 방법, 혜택 내용, 기간 등을 포함하여 설명해주세요.
                    """;
            case FINANCIAL_LOAN -> """
                    당신은 금융 대출 상담 전문가입니다.
                    대출 상품의 종류, 금리, 한도, 상환 조건 등을 상세히 안내해주세요.
                    사용자의 상황에 맞는 대출 상품을 추천하고 주의사항도 안내해주세요.
                    """;
            case AUTO_TRANSFER -> """
                    당신은 자동이체 서비스 안내 담당자입니다.
                    자동이체 설정, 변경, 해지 방법을 단계별로 상세히 안내해주세요.
                    필요한 정보와 주의사항도 함께 안내해주세요.
                    """;
            case TRANSACTION_HISTORY -> """
                    당신은 거래내역 조회 서비스 담당자입니다.
                    거래 내역 조회 방법, 기간별 조회, 상세 내역 확인 방법을 안내해주세요.
                    사용자가 원하는 거래 정보를 쉽게 찾을 수 있도록 도와주세요.
                    """;
            case ACTUAL_EXPENSE -> """
                    당신은 실비 보험 청구 전문 상담사입니다.
                    실비 보험 청구 절차, 필요 서류, 환급 기간 등을 상세히 안내해주세요.
                    청구 시 유의사항과 팁도 함께 제공해주세요.
                    """;
            case HOT_DEAL -> """
                    당신은 특가 상품 안내 담당자입니다.
                    현재 진행 중인 핫딜, 할인 상품, 특가 정보를 상세히 안내해주세요.
                    할인율, 혜택 내용, 구매 방법을 포함하여 설명해주세요.
                    """;
            case UNKNOWN -> """
                    당신은 친절한 고객 상담사입니다.
                    사용자의 질문을 이해하지 못했음을 정중히 안내하고,
                    다음 중 어떤 서비스를 원하는지 다시 물어봐주세요:
                    1. 카드추천 2. 카드안내 3. 이벤트안내 4. 금융대출
                    5. 자동이체 6. 거래내역 7. 실비 8. 핫딜구매
                    """;
        };
    }

    private double calculateConfidence(Intent intent) {
        return intent == Intent.UNKNOWN ? 0.0 : 0.85;
    }
}
