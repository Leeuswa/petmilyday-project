package com.petmilyday.service.impl.diagnosis;

import com.petmilyday.service.diagnosis.AiGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiGuideServiceImpl implements AiGuideService {

    private final ChatClient.Builder chatClientBuilder;

    // 사용자가 입력한 질문을 받아 AI 동물 가이드 답변을 생성하는 메서드
    @Override
    public String ask(String question) {

        if (question == null || question.isBlank()) {
            return "질문을 입력해주세요.";
        }

        String prompt = """
                너는 대한민국 반려동물 관리 가이드 AI입니다.

                반드시 한국어만 사용하세요.
                한자, 영어, 중국어, 일본어, 스페인어, 베트남어를 절대 사용하지 마세요.
                어려운 의학 용어보다 쉬운 표현을 사용하세요.

                수의학적 확정 진단이 아니라 일반적인 반려동물 관리 정보를 제공합니다.
                위험한 증상이나 응급 상황이 포함되어 있으면 동물병원 방문을 권장하세요.

                사용자 질문이 반려동물 관리와 관련이 없으면, 반려동물 관리와 관련된 질문을 입력해달라고 안내하세요.

                답변은 너무 길지 않게 작성하세요.
                반드시 아래 형식으로만 답변하세요.

                요약:
                관리 방법:
                주의사항:

                사용자 질문:
                %s
                """.formatted(question);

        try {
            String answer =
                    chatClientBuilder.build()
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();

            System.out.println("===== AI 가이드 응답 =====");
            System.out.println(answer);
            System.out.println("========================");

            return normalizeAnswer(answer);

        } catch (Exception e) {

            System.out.println("===== AI 가이드 오류 =====");
            e.printStackTrace();
            System.out.println("========================");

            return """
                    요약:
                    AI 가이드 응답을 불러오는 중 문제가 발생했습니다.

                    관리 방법:
                    잠시 후 다시 시도해주세요.
                    질문을 조금 더 구체적으로 입력하면 더 안정적인 답변을 받을 수 있습니다.

                    주의사항:
                    반려동물에게 구토, 설사, 무기력, 호흡 이상 같은 증상이 있다면 AI 답변을 기다리지 말고 동물병원 진료를 권장합니다.
                    """;
        }
    }

    private String normalizeAnswer(String answer) {

        if (answer == null || answer.isBlank()) {
            return fallbackAnswer();
        }

        answer = answer.trim();

        if (answer.isBlank()) {
            return fallbackAnswer();
        }

        return answer;
    }

    private String fallbackAnswer() {

        return """
                요약:
                반려동물 관리와 관련된 질문을 조금 더 구체적으로 입력해주세요.

                관리 방법:
                품종, 나이, 행동이 나타나는 상황, 식욕과 활동량 변화를 함께 적어주면 더 적절한 안내를 받을 수 있습니다.

                주의사항:
                구토, 설사, 무기력, 호흡 이상, 통증 반응이 있으면 동물병원 진료를 권장합니다.
                """;
    }
}