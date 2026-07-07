package com.petmilyday.service.impl.diagnosis;

import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.service.diagnosis.FoodGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodGuideServiceImpl implements FoodGuideService {

    private final ChatClient.Builder chatClientBuilder;
    private final PetProfileRepository petProfileRepository;

    // 등록된 반려동물 정보 + 특이사항을 반영해 AI가 사료를 추천하는 메서드
    @Override
    public String recommend(Long petId, String conditionText) {

        PetProfile pet = petProfileRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("반려동물 정보를 찾을 수 없습니다."));

        String condition = (conditionText == null || conditionText.isBlank())
                ? "특별한 증상이나 특이사항은 없습니다."
                : conditionText;

        String prompt = """
                너는 대한민국 반려동물 사료 추천 가이드 AI입니다.

                반드시 한국어만 사용하세요.
                한자, 영어, 중국어, 일본어를 사용하지 마세요.

                아래 반려동물 정보를 기준으로 적합한 사료 종류를 추천하세요.

                반려동물 이름: %s
                반려동물 종류: %s
                품종: %s
                나이: %s

                반려동물의 특이사항/증상:
                %s

                위 특이사항을 반드시 고려해서 추천 사료의 종류와 급여 시 주의할 점을 알려주세요.

                답변은 너무 길지 않게 작성하세요.
                반드시 아래 형식으로만 답변하세요.

                요약:
                추천 사료 종류:
                급여 시 주의사항:
                """.formatted(
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                condition
        );

        try {
            return chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {

            System.out.println("===== AI 사료 추천 오류 =====");
            e.printStackTrace();
            System.out.println("============================");

            return """
                    요약:
                    AI 사료 추천 응답을 불러오는 중 문제가 발생했습니다.

                    추천 사료 종류:
                    잠시 후 다시 시도해주세요.

                    급여 시 주의사항:
                    반려동물에게 특이 증상이 있다면 사료 변경 전 동물병원 상담을 권장합니다.
                    """;
        }
    }
}
