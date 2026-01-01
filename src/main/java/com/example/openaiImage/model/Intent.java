package com.example.openaiImage.model;

public enum Intent {
    CARD_RECOMMENDATION("카드추천", "사용자에게 적합한 카드를 추천합니다"),
    CARD_GUIDE("카드안내", "카드 상품에 대한 정보를 안내합니다"),
    EVENT_INFO("이벤트안내", "진행 중인 이벤트 정보를 안내합니다"),
    FINANCIAL_LOAN("금융대출", "대출 상품 및 조건을 안내합니다"),
    AUTO_TRANSFER("자동이체", "자동이체 설정 및 관리를 도와드립니다"),
    TRANSACTION_HISTORY("거래내역", "거래 내역을 조회하고 관리합니다"),
    ACTUAL_EXPENSE("실비", "실비 보험 및 청구 관련 안내를 제공합니다"),
    HOT_DEAL("핫딜구매", "특가 상품 정보를 안내합니다"),
    UNKNOWN("알수없음", "의도를 파악할 수 없습니다");

    private final String koreanName;
    private final String description;

    Intent(String koreanName, String description) {
        this.koreanName = koreanName;
        this.description = description;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getDescription() {
        return description;
    }
}
