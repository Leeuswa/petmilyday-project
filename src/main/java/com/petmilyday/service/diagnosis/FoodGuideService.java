package com.petmilyday.service.diagnosis;

public interface FoodGuideService {

    String recommend(Long petId, String conditionText);
}
