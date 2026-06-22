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

        String fixedAnswer = defaultAnswer(question);

        if (!fixedAnswer.isBlank()) {
            return fixedAnswer;
        }

        String prompt = """
                너는 대한민국 반려동물 관리 가이드 AI입니다.

                반드시 한국어만 사용하세요.
                한자, 영어, 중국어, 일본어, 스페인어, 베트남어를 절대 사용하지 마세요.
                어려운 의학 용어보다 쉬운 표현을 사용하세요.

                수의학적 확정 진단이 아니라 일반적인 반려동물 관리 정보를 제공합니다.
                위험한 증상이나 응급 상황이 포함되어 있으면 동물병원 방문을 권장하세요.

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

        answer = answer
                .replaceAll("[\\u4E00-\\u9FFF]", "")
                .replaceAll("[\\u3040-\\u30FF]", "")
                .replaceAll("[\\u0E00-\\u0E7F]", "")
                .replaceAll("[a-zA-Z]", "")
                .trim();

        if (answer.isBlank()
                || answer.length() < 30
                || answer.contains("温度")
                || answer.contains("功能")
                || answer.contains("agua")
                || answer.contains("juice")
                || answer.contains("Impl")
                || !answer.contains("요약")
                || !answer.contains("관리")
                || !answer.contains("주의")) {

            return fallbackAnswer();
        }

        if (!answer.contains("요약:")) {
            answer = "요약:\n" + answer;
        }

        return answer;
    }

    private String defaultAnswer(String question) {

        if (question == null) {
            return "";
        }

        if (question.contains("날씨")
                || question.contains("저녁")
                || question.contains("뭐 먹")
                || question.contains("공부")
                || question.contains("게임")
                || question.contains("안녕하세요")
                || question.contains("심심")
                || question.contains("귀여워")
                || question.contains("귀엽")
                || question.contains("최고예요")) {

            return """
            요약:
            반려동물 관리와 관련된 질문을 입력해주세요.

            관리 방법:
            품종, 나이, 행동, 식욕, 활동량, 배변 상태처럼 반려동물과 관련된 내용을 함께 적으면 더 적절한 안내를 받을 수 있습니다.

            주의사항:
            구토, 설사, 무기력, 호흡 이상, 통증 반응이 있으면 동물병원 진료를 권장합니다.
            """;
        }

        if (question.contains("화장실")
                || question.contains("모래")
                || question.contains("배변")
                || question.contains("소변")
                || question.contains("대변")) {

            return """
                    요약:
                    고양이 화장실은 고양이 수보다 1개 더 준비하는 것이 좋습니다.

                    관리 방법:
                    고양이가 1마리라면 화장실 2개, 2마리라면 3개처럼 배치하는 것을 권장합니다.
                    화장실은 조용하고 접근하기 쉬운 위치에 두고, 모래는 매일 청소해 주세요.
                    고양이가 화장실 사용을 피한다면 위치, 청결 상태, 모래 종류를 확인하는 것이 좋습니다.

                    주의사항:
                    갑자기 배변 실수를 하거나 화장실을 자주 들락거리는데 소변을 못 본다면 동물병원 진료를 권장합니다.
                    """;
        }

        if (question.contains("우다다")
                || question.contains("밤마다")
                || question.contains("밤에")
                || question.contains("활발")
                || question.contains("잠을 못")) {

            return """
                    요약:
                    고양이가 밤에 우다다를 하는 것은 에너지가 남아 있거나 낮 동안 활동량이 부족할 때 자주 나타날 수 있습니다.

                    관리 방법:
                    잠들기 전 10~15분 정도 낚싯대 장난감이나 공놀이로 충분히 놀아주세요.
                    낮 동안에도 짧게 여러 번 놀아주면 밤에 갑자기 뛰어다니는 행동을 줄이는 데 도움이 됩니다.
                    놀이 후에는 간식이나 휴식으로 마무리해 흥분을 낮춰주는 것이 좋습니다.

                    주의사항:
                    갑자기 밤 행동이 심해졌거나 울음, 식욕 저하, 배변 이상이 함께 나타나면 건강 문제일 수 있으므로 동물병원 상담을 권장합니다.
                    """;
        }

        if (question.contains("스크래처")
                || question.contains("소파")
                || question.contains("가구")
                || question.contains("긁어요")
                || question.contains("긁어")) {

            return """
                    요약:
                    고양이가 가구를 긁는 것은 발톱 관리, 영역 표시, 스트레스 해소와 관련된 자연스러운 행동입니다.

                    관리 방법:
                    고양이가 자주 긁는 위치 근처에 스크래처를 배치해 주세요.
                    세로형, 가로형, 박스형 등 다양한 스크래처를 시도해 보고 좋아하는 재질을 찾는 것이 좋습니다.
                    스크래처를 사용했을 때 간식이나 칭찬으로 긍정적인 경험을 만들어 주세요.

                    주의사항:
                    혼내거나 억지로 못 긁게 하기보다는 대체할 수 있는 스크래처를 제공하는 것이 좋습니다.
                    갑자기 과하게 긁거나 불안 행동이 함께 보이면 스트레스 원인을 확인해 주세요.
                    """;
        }

        if (question.contains("양치")
                || question.contains("치석")
                || question.contains("입냄새")
                || question.contains("구강")
                || question.contains("이빨")
                || question.contains("치아")) {

            return """
                    요약:
                    반려동물의 치아 관리는 입냄새, 치석, 잇몸 질환 예방에 중요합니다.

                    관리 방법:
                    처음부터 칫솔을 사용하기보다 손가락 거즈나 반려동물용 치약 냄새에 익숙해지게 해주세요.
                    짧은 시간부터 시작해 점차 양치 시간을 늘리는 것이 좋습니다.
                    사람용 치약은 사용하지 말고 반드시 반려동물용 제품을 사용해야 합니다.

                    주의사항:
                    잇몸 출혈, 심한 입냄새, 사료를 씹기 어려워하는 모습이 있으면 동물병원 진료를 권장합니다.
                    """;
        }

        if (question.contains("산책")
                || question.contains("운동")
                || question.contains("밖에")
                || question.contains("걷기")
                || question.contains("산책을 싫어")) {

            return """
                    요약:
                    산책은 반려견의 체력 관리와 스트레스 해소에 도움이 됩니다.

                    관리 방법:
                    처음에는 짧고 조용한 코스부터 시작해 천천히 적응시키는 것이 좋습니다.
                    산책을 싫어한다면 억지로 끌고 가기보다 간식과 칭찬으로 긍정적인 경험을 만들어 주세요.
                    날씨가 너무 덥거나 추운 시간대는 피하고, 반려견의 체력에 맞춰 시간을 조절해 주세요.

                    주의사항:
                    산책 중 절뚝거림, 심한 헐떡임, 갑작스러운 거부 반응이 있다면 건강 상태를 확인해야 합니다.
                    """;
        }

        if (question.contains("낯선")
                || question.contains("사람")
                || question.contains("짖")
                || question.contains("경계")
                || question.contains("무서워")) {

            return """
                    요약:
                    반려견이 낯선 사람에게 짖는 것은 경계심, 불안, 사회화 부족과 관련될 수 있습니다.

                    관리 방법:
                    낯선 사람과의 거리를 충분히 두고, 조용히 있을 때 간식과 칭찬을 제공해 주세요.
                    갑자기 가까이 다가가게 하기보다 천천히 적응할 시간을 주는 것이 중요합니다.
                    반복적인 긍정 경험을 통해 낯선 상황에 대한 불안을 줄일 수 있습니다.

                    주의사항:
                    공격적인 행동이 심하거나 보호자가 통제하기 어렵다면 전문가 상담을 권장합니다.
                    """;
        }

        if (question.contains("털빠짐")
                || question.contains("털이 빠")
                || question.contains("빗질")
                || question.contains("털 관리")
                || question.contains("털갈이")) {

            return """
                    요약:
                    털 빠짐은 계절, 품종, 피부 상태에 따라 달라질 수 있으며 꾸준한 관리가 필요합니다.

                    관리 방법:
                    반려동물의 털 종류에 맞는 빗을 사용해 정기적으로 빗질해 주세요.
                    목욕을 너무 자주 하면 피부가 건조해질 수 있으므로 적절한 주기를 유지하는 것이 좋습니다.
                    사료 변경, 스트레스, 피부 자극이 있었는지도 함께 확인해 주세요.

                    주의사항:
                    특정 부위만 털이 빠지거나 피부가 붉고 가려워한다면 피부 질환 가능성이 있어 동물병원 진료를 권장합니다.
                    """;
        }

        if (question.contains("목욕")
                || question.contains("냄새")
                || question.contains("샴푸")
                || question.contains("씻")) {

            return """
                    요약:
                    목욕은 피부와 털을 깨끗하게 유지하는 데 도움이 되지만 너무 자주 하면 피부가 건조해질 수 있습니다.

                    관리 방법:
                    반려동물 전용 샴푸를 사용하고, 목욕 후에는 털과 피부를 충분히 말려 주세요.
                    냄새가 심하다면 귀, 입, 피부 상태를 함께 확인하는 것이 좋습니다.
                    목욕 주기는 반려동물의 피부 상태와 생활 환경에 맞춰 조절해야 합니다.

                    주의사항:
                    목욕 후 피부가 붉어지거나 가려움이 심해지면 사용한 제품을 중단하고 동물병원 상담을 권장합니다.
                    """;
        }

        if (question.contains("장난감")
                || question.contains("놀아")
                || question.contains("놀이")
                || question.contains("질려")
                || question.contains("놀기")) {

            return """
            요약:
            반려동물이 장난감에 금방 질리는 경우에는 놀이 방식과 장난감 종류를 바꿔주는 것이 도움이 됩니다.

            관리 방법:
            장난감을 한 번에 모두 꺼내두기보다 몇 개씩 번갈아 제공해 주세요.
            짧게 여러 번 놀아주고, 놀이가 끝나면 간식이나 칭찬으로 좋은 경험을 만들어 주세요.
            공, 터그 장난감, 노즈워크, 낚싯대 장난감처럼 다양한 놀이를 시도해 보는 것도 좋습니다.

            주의사항:
            장난감을 삼키거나 물어뜯어 조각을 먹지 않도록 놀이 중에는 보호자가 지켜봐 주세요.
            """;
        }

        if (question.contains("창밖")
                || question.contains("창문")
                || question.contains("새벽")
                || question.contains("깨워")
                || question.contains("계속 보고")) {

            return """
            요약:
            고양이가 창밖을 오래 보거나 새벽에 보호자를 깨우는 행동은 호기심, 에너지, 관심 요구와 관련될 수 있습니다.

            관리 방법:
            낮 시간에 놀이 시간을 늘려 에너지를 충분히 쓰게 해주세요.
            창가에 안전한 휴식 공간을 만들어 주되, 방충망이나 창문 안전 상태를 꼭 확인해 주세요.
            새벽에 깨울 때 바로 반응하면 행동이 반복될 수 있으므로 일정한 생활 리듬을 만들어 주는 것이 좋습니다.

            주의사항:
            갑자기 울음이 심해졌거나 식욕 저하, 배변 이상이 함께 나타나면 건강 문제일 수 있으므로 동물병원 상담을 권장합니다.
            """;
        }

        if (question.contains("차")
                || question.contains("자동차")
                || question.contains("이동장")
                || question.contains("이동")
                || question.contains("멀미")) {

            return """
            요약:
            반려동물이 차를 타면 긴장하거나 불편해하는 것은 이동 환경에 익숙하지 않거나 멀미 때문일 수 있습니다.

            관리 방법:
            처음에는 정차된 차 안에서 짧게 머무는 연습부터 시작해 주세요.
            이동장이나 안전벨트를 사용해 안정감을 주고, 짧은 거리부터 천천히 적응시키는 것이 좋습니다.
            차를 타기 직전에는 과식하지 않도록 하고, 이동 후에는 조용히 쉴 수 있게 해주세요.

            주의사항:
            침 흘림, 반복 구토, 심한 떨림이 계속되면 동물병원에 상담해 보는 것이 좋습니다.
            """;
        }

        if (question.contains("새로운 가족")
                || question.contains("새 가족")
                || question.contains("가까이 가지")
                || question.contains("친해")
                || question.contains("적응")) {

            return """
            요약:
            반려동물이 새로운 가족에게 바로 다가가지 않는 것은 자연스러운 적응 과정일 수 있습니다.

            관리 방법:
            억지로 안거나 가까이 데려가기보다 스스로 다가올 시간을 주세요.
            간식, 장난감, 조용한 목소리로 좋은 경험을 만들어 주는 것이 좋습니다.
            처음에는 짧은 시간만 함께 있게 하고, 반려동물이 숨을 수 있는 안전한 공간도 마련해 주세요.

            주의사항:
            공격성, 심한 떨림, 식욕 저하가 오래 지속되면 스트레스가 큰 상태일 수 있으므로 전문가 상담을 권장합니다.
            """;
        }

        if (question.contains("소리")
                || question.contains("놀라")
                || question.contains("도망")
                || question.contains("특정 장소")
                || question.contains("지나가려고 하지")) {

            return """
            요약:
            특정 소리나 장소를 무서워하는 행동은 과거 경험, 낯선 자극, 불안감과 관련될 수 있습니다.

            관리 방법:
            무서워하는 자극을 억지로 마주하게 하지 말고, 거리를 두고 천천히 적응시켜 주세요.
            조용히 안정된 상태를 보일 때 간식과 칭찬을 주면 도움이 됩니다.
            보호자가 침착하게 행동하고, 반려동물이 피할 수 있는 안전한 공간을 마련해 주세요.

            주의사항:
            공포 반응이 심하거나 공격 행동으로 이어진다면 전문가 상담을 권장합니다.
            """;
        }

        if (question.contains("박스")
                || question.contains("상자")
                || question.contains("숨숨집")
                || question.contains("숨어")
                || question.contains("숨는")) {

            return """
            요약:
            고양이가 박스나 숨을 수 있는 공간을 좋아하는 것은 안정감을 느끼기 위한 자연스러운 행동입니다.

            관리 방법:
            조용하고 안전한 곳에 박스나 숨숨집을 마련해 주세요.
            억지로 꺼내기보다 스스로 나올 수 있게 기다리는 것이 좋습니다.
            새로운 환경에서는 숨을 공간이 있으면 스트레스를 줄이는 데 도움이 됩니다.

            주의사항:
            평소와 다르게 계속 숨어 있고 식욕 저하나 무기력이 함께 나타나면 건강 문제일 수 있으므로 동물병원 상담을 권장합니다.
            """;
        }

        if (question.contains("자는 위치")
                || question.contains("잠자리")
                || question.contains("자는 자리")
                || question.contains("휴식 공간")
                || question.contains("자주 바꿔")) {

            return """
            요약:
            반려동물이 잠자는 자리를 자주 바꾸는 것은 온도, 소음, 편안함, 주변 환경에 영향을 받을 수 있습니다.

            관리 방법:
            조용하고 온도가 적당한 곳에 편안한 방석이나 침대를 마련해 주세요.
            한 곳만 강요하기보다 여러 휴식 공간을 제공하는 것도 좋습니다.
            자주 쉬는 위치 주변에 위험한 물건이 없는지 확인해 주세요.

            주의사항:
            잠을 잘 못 자거나 통증, 무기력, 식욕 저하가 함께 나타나면 건강 상태를 확인해야 합니다.
            """;
        }

        if (question.contains("물")
                || question.contains("음수")
                || question.contains("마시")) {

            return """
                    요약:
                    반려동물이 물을 잘 마시지 않는 경우에는 음수량을 자연스럽게 늘리는 관리가 필요합니다.

                    관리 방법:
                    깨끗한 물을 자주 갈아주고, 여러 위치에 물그릇을 놓아 접근성을 높여주세요.
                    습식 사료를 함께 급여하거나 반려동물용 정수기를 사용하는 것도 도움이 될 수 있습니다.

                    주의사항:
                    하루 이상 물을 거의 마시지 않거나 구토, 설사, 무기력이 함께 나타나면 동물병원 진료를 권장합니다.
                    """;
        }

        if (question.contains("사료")
                || question.contains("밥")
                || question.contains("먹")) {

            return """
                    요약:
                    반려동물이 사료를 잘 먹지 않는 경우에는 식욕 저하의 원인을 확인해야 합니다.

                    관리 방법:
                    사료가 오래되었거나 갑자기 바뀌지 않았는지 확인하고, 급여 환경을 조용하게 만들어주세요.
                    평소보다 먹는 양이 줄었다면 하루 정도 식사량과 활동량을 관찰하세요.

                    주의사항:
                    식욕 저하가 하루 이상 지속되거나 구토, 설사, 무기력이 함께 나타나면 동물병원 진료를 권장합니다.
                    """;
        }

        if (question.contains("분리불안")
                || question.contains("불안")
                || question.contains("울")) {

            return """
                    요약:
                    분리불안은 반려동물이 보호자와 떨어졌을 때 불안 행동을 보이는 상태입니다.

                    관리 방법:
                    짧은 외출부터 천천히 연습하고, 혼자 있는 동안 장난감이나 간식을 활용해 안정감을 주세요.
                    외출 전후에 과도하게 흥분시키지 않는 것도 도움이 됩니다.

                    주의사항:
                    짖음, 파괴 행동, 배변 실수가 심하거나 장기간 지속되면 전문가 상담을 권장합니다.
                    """;
        }

        if (question.contains("노령")
                || question.contains("나이")
                || question.contains("늙")) {

            return """
                    요약:
                    노령 반려동물은 활동량, 식사량, 관절 건강, 정기 검진 관리가 중요합니다.

                    관리 방법:
                    무리한 운동보다는 가벼운 산책과 편안한 휴식 공간을 제공해주세요.
                    체중 변화, 식욕 변화, 배변 상태를 꾸준히 확인하는 것이 좋습니다.

                    주의사항:
                    갑작스러운 체중 감소, 심한 무기력, 통증 반응이 보이면 동물병원 진료를 권장합니다.
                    """;
        }

        if (question.contains("피부")
                || question.contains("긁")
                || question.contains("털")) {

            return """
                    요약:
                    피부를 자주 긁거나 털이 빠지는 경우에는 피부 자극이나 알레르기 가능성을 살펴봐야 합니다.

                    관리 방법:
                    목욕이나 빗질을 과하게 하지 말고, 피부가 붉어진 부위를 계속 핥지 않도록 관찰해주세요.
                    최근 바뀐 사료, 간식, 샴푸, 환경이 있는지도 확인하는 것이 좋습니다.

                    주의사항:
                    붉어짐, 상처, 진물, 심한 가려움이 지속되면 동물병원 진료를 권장합니다.
                    """;
        }

        return "";
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