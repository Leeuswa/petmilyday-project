package com.petmilyday.service.impl.geocoding;

import com.petmilyday.dto.geocoding.GeoPointDTO;
import com.petmilyday.service.geocoding.GeocodingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// 카카오 로컬 API(주소 검색)로 주소 문자열을 좌표로 변환한다.
// 지도 표시용 JavaScript 키(kakao.map.api-key)는 KA Header가 없으면 서버 호출이 거부되므로
// 별도의 REST API 키(kakao.rest-api-key)를 사용한다.
@Service
@Log4j2
public class GeocodingServiceImpl implements GeocodingService {

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://dapi.kakao.com")
                    .build();

    @Override
    public GeoPointDTO geocode(String address) {

        if (address == null || address.isBlank()) {
            throw new RuntimeException("주소를 입력해주세요.");
        }

        Map response =
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v2/local/search/address.json")
                                .queryParam("query", address)
                                .build())
                        .header("Authorization", "KakaoAK " + kakaoApiKey.trim())
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                errorResponse -> errorResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.warn("카카오 주소 검색 오류 - address: {}, body: {}", address, errorBody);
                                            return Mono.error(
                                                    new RuntimeException("주소를 좌표로 변환하는 중 오류가 발생했습니다.")
                                            );
                                        })
                        )
                        .bodyToMono(Map.class)
                        .block();

        if (response == null) {
            throw new RuntimeException("주소 검색 응답이 없습니다.");
        }

        List<Map<String, Object>> documents = (List<Map<String, Object>>) response.get("documents");

        if (documents == null || documents.isEmpty()) {
            throw new RuntimeException("입력하신 주소로 좌표를 찾을 수 없습니다. 주소를 다시 확인해주세요.");
        }

        Map<String, Object> first = documents.get(0);

        BigDecimal longitude = new BigDecimal((String) first.get("x"));
        BigDecimal latitude = new BigDecimal((String) first.get("y"));

        return GeoPointDTO.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
