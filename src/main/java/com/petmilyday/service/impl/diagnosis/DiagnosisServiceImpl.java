package com.petmilyday.service.impl.diagnosis;

import com.petmilyday.entity.diagnosis.DiagnosisHistory;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.repository.diagnosis.DiagnosisHistoryRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.service.diagnosis.DiagnosisService;
import com.petmilyday.service.product.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final ChatClient.Builder chatClientBuilder;

    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final MemberRepository memberRepository;
    private final PetProfileRepository petProfileRepository;
    private final S3UploadService s3UploadService;

    // AI 자가진단 수행 및 이력 저장
    @Override
    public DiagnosisHistory diagnose(
            Long memberId,
            Long petId,
            String symptomText,
            MultipartFile image
    ) throws IOException {

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("회원 없음"));

        PetProfile pet =
                petProfileRepository.findById(petId)
                        .orElseThrow(() -> new RuntimeException("반려동물 없음"));

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = s3UploadService.uploadFile(image);
            } catch (Exception e) {
                System.out.println("===== S3 이미지 업로드 오류 =====");
                e.printStackTrace();
                System.out.println("==============================");

                imageUrl = null;
            }
        }

        if (symptomText == null || symptomText.isBlank()) {
            symptomText = "증상 정보가 입력되지 않았습니다.";
        }

        if (symptomText.length() > 500) {
            symptomText = symptomText.substring(0, 500);
        }

        String prompt = """
                너는 대한민국 반려동물 자가진단 보조 AI입니다.

                반드시 한국어만 사용하세요.
                한자, 영어, 중국어, 일본어를 사용하지 마세요.

                아래 반려동물 정보가 가장 중요합니다.
                사용자 입력에 다른 동물명이 있어도 반드시 아래 반려동물 정보를 기준으로 진단하세요.

                반려동물 이름: %s
                반려동물 종류: %s
                품종: %s
                나이: %s

                증상에 맞는 구체적인 질환명을 자유롭게 작성하세요.
                반려동물 증상이 아니거나 확실하지 않으면 질환명을 "판단불가"라고 답하세요.

                심각도는 LOW, MEDIUM, HIGH 중 하나만 사용하세요.

                판단 기준:
                LOW = 증상이 가볍고 식욕과 활력이 정상인 경우
                MEDIUM = 증상이 며칠 지속되거나 붉어짐, 긁음, 식욕 감소가 있는 경우
                HIGH = 피 섞인 설사, 반복 구토, 호흡 이상, 물 거부, 움직이지 않음, 심한 무기력

                사용자 입력이 반려동물 증상이 아니면 판단불가로 답하세요.

                사용자 입력:
                %s

                반드시 아래 형식만 출력하세요.

                질환:
                심각도:
                권고사항:
                """.formatted(
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                symptomText
        );

        String aiResult;
        boolean aiError = false;

        try {
            aiResult =
                    chatClientBuilder.build()
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();

            System.out.println("===== AI 응답 =====");
            System.out.println(aiResult);
            System.out.println("==================");

        } catch (Exception e) {

            System.out.println("===== AI 자가진단 오류 =====");
            e.printStackTrace();
            System.out.println("==========================");

            aiError = true;
            aiResult = "";
        }

        String disease = parseValue(aiResult, "질환:");
        String severity = parseValue(aiResult, "심각도:");
        String recommend = parseValue(aiResult, "권고사항:");

        if (aiError) {
            disease = "판단불가";
            severity = "LOW";
            recommend = "AI 진단 응답을 불러오는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        } else {
            // 질환명은 Claude가 자유롭게 작성한 내용을 그대로 사용한다.
            // (형식이 깨져 비어있을 때만 "판단불가" 처리)
            if (disease == null || disease.isBlank()) {
                disease = "판단불가";
            }

            // 심각도는 화면에서 색상 배지로 쓰이므로 LOW/MEDIUM/HIGH 3단계로 고정한다.
            // AI가 형식을 벗어난 경우에만 증상 텍스트 기반 추정으로 보완한다.
            if (!isValidSeverity(severity)) {
                severity = normalizeSeverity(severity, symptomText);
            }

            if ("판단불가".equals(disease)) {
                severity = "LOW";
            }

            if (recommend == null || recommend.isBlank()) {
                recommend = "증상이 지속되거나 악화되면 동물병원 진료를 권장합니다.";
            }
        }

        DiagnosisHistory history =
                DiagnosisHistory.builder()
                        .member(member)
                        .pet(pet)
                        .symptomText(symptomText)
                        .imageUrl(imageUrl)
                        .resultDisease(disease)
                        .resultSeverity(severity)
                        .resultRecommend(recommend)
                        .createdAt(LocalDateTime.now())
                        .build();

        return diagnosisHistoryRepository.save(history);
    }

    // 진단 이력 페이징 조회
    @Override
    public Page<DiagnosisHistory> getHistory(Long memberId, Pageable pageable) {

        return diagnosisHistoryRepository
                .findByMember_IdOrderByCreatedAtDesc(memberId, pageable);
    }

    private static final java.util.Set<String> ALLOWED_SEVERITIES = java.util.Set.of(
            "LOW", "MEDIUM", "HIGH"
    );

    private boolean isValidSeverity(String severity) {
        return severity != null && ALLOWED_SEVERITIES.contains(severity.trim().toUpperCase());
    }

    private String parseValue(String text, String key) {

        if (text == null || text.isBlank()) {
            return "";
        }

        for (String line : text.split("\\R")) {
            if (line.trim().startsWith(key)) {
                return line.replace(key, "").trim();
            }
        }

        return "";
    }

    private String normalizeSeverity(String severity, String symptomText) {

        String text = "";

        if (severity != null) {
            text += severity + " ";
        }

        if (symptomText != null) {
            text += symptomText;
        }

        text = text.toUpperCase();

        if (text.contains("피가 섞")
                || text.contains("혈변")
                || text.contains("반복 구토")
                || text.contains("계속 구토")
                || text.contains("물을 안")
                || text.contains("물도 안")
                || text.contains("물을 거의")
                || text.contains("움직이지")
                || text.contains("무기력")
                || text.contains("축 처")
                || text.contains("호흡")
                || text.contains("숨을 빠르게")
                || text.contains("힘없이")) {

            return "HIGH";
        }

        if (text.contains("HIGH")) {
            return "HIGH";
        }

        if (text.contains("며칠")
                || text.contains("3일")
                || text.contains("이틀")
                || text.contains("일주일")
                || text.contains("계속")
                || text.contains("심해")
                || text.contains("식욕")
                || text.contains("붉")
                || text.contains("빨갛")
                || text.contains("긁")
                || text.contains("털이 빠")
                || text.contains("냄새")
                || text.contains("구토")) {

            return "MEDIUM";
        }

        if (text.contains("MEDIUM")) {
            return "MEDIUM";
        }

        return "LOW";
    }

}