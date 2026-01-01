package com.example.openaiImage.service;

import com.example.openaiImage.model.Intent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class IntentClassifierService {

    private final ChatModel chatModel;
    private final HybridSearchService hybridSearchService;

    public IntentClassifierService(ChatModel chatModel, HybridSearchService hybridSearchService) {
        this.chatModel = chatModel;
        this.hybridSearchService = hybridSearchService;
    }

    public Intent classifyIntent(String userMessage) {
        // GPT를 메인으로 사용
        String promptText = buildClassificationPrompt(userMessage);

        String response = chatModel.call(
                new Prompt(
                        promptText,
                        OpenAiChatOptions.builder()
                                .withModel("gpt-4o")
                                .withTemperature(0.3)
                                .build()
                )).getResult().getOutput().getContent();

        Intent gptResult = parseIntentFromResponse(response.trim());

        // GPT가 분류하지 못한 경우에만 Elasticsearch 사용
        if (gptResult == Intent.UNKNOWN) {
            return hybridSearchService.searchIntent(userMessage);
        }

        return gptResult;
    }

    private String buildClassificationPrompt(String userMessage) {
        return """
                당신은 사용자의 의도를 정확하게 분류하는 전문가입니다.
                사용자의 질문을 분석하여 다음 8개의 카테고리 중 하나로 분류해주세요.

                카테고리:
                1. 카드추천 - 카드 추천, 어떤 카드가 좋은지, 나에게 맞는 카드 등
                2. 카드안내 - 특정 카드 정보, 카드 혜택, 카드 사용법, 연회비 등
                3. 이벤트안내 - 진행중인 이벤트, 프로모션, 행사 정보 등
                4. 금융대출 - 대출 상품, 대출 조건, 이자율, 신용대출 등
                5. 자동이체 - 자동이체 설정, 등록, 해지, 변경 등
                6. 거래내역 - 거래 조회, 사용 내역, 결제 내역, 입출금 내역 등
                7. 실비 - 실비 보험, 보험 청구, 의료비 환급 등
                8. 핫딜구매 - 특가 상품, 할인 상품, 핫딜 정보 등

                사용자 질문: "%s"

                위 질문에 가장 적합한 카테고리 하나만 다음 중에서 정확히 선택하여 응답해주세요:
                카드추천, 카드안내, 이벤트안내, 금융대출, 자동이체, 거래내역, 실비, 핫딜구매

                카테고리 이름만 정확히 답변해주세요. 다른 설명은 필요 없습니다.
                """.formatted(userMessage);
    }

    private Intent parseIntentFromResponse(String response) {
        String normalized = response.replace(" ", "").toLowerCase();

        if (normalized.contains("카드추천")) {
            return Intent.CARD_RECOMMENDATION;
        } else if (normalized.contains("카드안내")) {
            return Intent.CARD_GUIDE;
        } else if (normalized.contains("이벤트안내") || normalized.contains("이벤트")) {
            return Intent.EVENT_INFO;
        } else if (normalized.contains("금융대출") || normalized.contains("대출")) {
            return Intent.FINANCIAL_LOAN;
        } else if (normalized.contains("자동이체")) {
            return Intent.AUTO_TRANSFER;
        } else if (normalized.contains("거래내역") || normalized.contains("거래")) {
            return Intent.TRANSACTION_HISTORY;
        } else if (normalized.contains("실비")) {
            return Intent.ACTUAL_EXPENSE;
        } else if (normalized.contains("핫딜구매") || normalized.contains("핫딜")) {
            return Intent.HOT_DEAL;
        } else {
            return Intent.UNKNOWN;
        }
    }
}
