package com.petmilyday.domain.used;

public enum ItemCondition {

    NEW("새상품"),
    LIKE_NEW("거의 새것"),
    USED("사용감 있음");

    private final String description;

    ItemCondition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}