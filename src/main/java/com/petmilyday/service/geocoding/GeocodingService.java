package com.petmilyday.service.geocoding;

import com.petmilyday.dto.geocoding.GeoPointDTO;

public interface GeocodingService {

    // 주소 문자열을 위도/경도로 변환
    GeoPointDTO geocode(String address);
}
