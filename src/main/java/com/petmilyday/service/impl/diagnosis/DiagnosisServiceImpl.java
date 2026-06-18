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

        if (symptomText == null) {
            symptomText = "";
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

                진단 가능한 질환명은 아래 중 하나만 사용하세요.
                피부염, 알레르기성 피부염, 외이염, 결막염, 위장염, 장염, 식욕부진, 스트레스, 판단불가

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

        String recommend;

        if (aiError) {
            disease = "판단불가";
            severity = "LOW";
            recommend = "AI 진단 응답을 불러오는 중 문제가 발생했습니다. Ollama 실행 상태를 확인하거나 잠시 후 다시 시도해주세요.";
        } else {
            disease = normalizeDisease(disease, symptomText);
            severity = normalizeSeverity(severity, symptomText);

            if ("판단불가".equals(disease)) {
                severity = "LOW";
            }

            recommend = normalizeRecommend(disease);
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

    private String normalizeDisease(String disease, String symptomText) {

        String text = "";

        if (disease != null) {
            text += disease + " ";
        }

        if (symptomText != null) {
            text += symptomText;
        }

        if (text.isBlank()) {
            return "판단불가";
        }

        if (text.contains("안녕하세요")
                || text.contains("날씨")
                || text.contains("좋네요")
                || text.contains("귀여")
                || text.contains("예뻐")
                || text.contains("사랑스러")
                || text.contains("배고파요")
                || text.contains("저를 좋아")
                || text.contains("산책 다녀왔")) {

            return "판단불가";
        }

        if (text.contains("판단불가")) {
            return "판단불가";
        }

        if (text.contains("귀를 긁")
                || text.contains("귀가")
                || text.contains("귀에서")
                || text.contains("귀 안")
                || text.contains("외이")
                || text.contains("머리를 흔")
                || text.contains("귀 냄새")
                || text.contains("귀를 자주")) {

            return "외이염";
        }

        if (text.contains("피부")
                || text.contains("빨갛")
                || text.contains("붉")
                || text.contains("긁")
                || text.contains("가려")
                || text.contains("핥")
                || text.contains("각질")
                || text.contains("털이 빠")) {

            return "알레르기성 피부염";
        }

        if (text.contains("눈")
                || text.contains("눈물")
                || text.contains("충혈")
                || text.contains("비비")) {

            return "결막염";
        }

        if (text.contains("구토")
                || text.contains("토했")
                || text.contains("토하")) {

            return "위장염";
        }

        if (text.contains("설사")
                || text.contains("혈변")
                || text.contains("피가")) {

            return "장염";
        }

        if (text.contains("밥을 안")
                || text.contains("사료")
                || text.contains("식욕")
                || text.contains("먹지")
                || text.contains("안 먹")
                || text.contains("먹는 양")) {

            return "식욕부진";
        }

        if (text.contains("스트레스")
                || text.contains("불안")
                || text.contains("숨어")
                || text.contains("숨는")
                || text.contains("낯선")
                || text.contains("이사")) {

            return "스트레스";
        }

        return "판단불가";
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

    private String normalizeRecommend(String disease) {

        if (disease == null || disease.equals("판단불가")) {
            return "반려동물의 증상을 더 구체적으로 입력해주세요.";
        }

        if (disease.equals("알레르기성 피부염")) {
            return "피부를 긁거나 붉어지는 증상이 지속되면 자극 원인을 피하고 동물병원 진료를 권장합니다.";
        }

        if (disease.equals("외이염")) {
            return "귀를 자주 긁거나 냄새가 나면 귀를 만지지 말고 동물병원에서 귀 상태를 확인받는 것이 좋습니다.";
        }

        if (disease.equals("결막염")) {
            return "눈물, 충혈, 눈 비빔 증상이 지속되면 눈 주변을 청결히 하고 동물병원 진료를 권장합니다.";
        }

        if (disease.equals("위장염") || disease.equals("장염")) {
            return "구토나 설사가 반복되거나 식욕이 떨어지면 탈수 위험이 있으므로 빠른 진료를 권장합니다.";
        }

        if (disease.equals("식욕부진")) {
            return "식욕 저하가 하루 이상 지속되거나 무기력이 함께 나타나면 동물병원 진료를 권장합니다.";
        }

        if (disease.equals("스트레스")) {
            return "환경 변화나 불안 요인을 줄이고, 이상 행동이 지속되면 전문가 상담을 권장합니다.";
        }

        return "증상이 지속되거나 악화되면 동물병원 진료를 권장합니다.";
    }
}