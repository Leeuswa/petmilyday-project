package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.geocoding.GeoPointDTO;
import com.petmilyday.dto.hospital.HospitalHoursDTO;
import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.geocoding.GeocodingService;
import com.petmilyday.service.hospital.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final GeocodingService geocodingService;

    // 병원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<HospitalResponseDTO> hospitalList(HospitalRequestDTO requestDTO) {

        List<Hospital> hospitals = hospitalRepository.searchHospitals(requestDTO);

        return hospitals.stream()
                .map(hospital -> modelMapper.map(hospital, HospitalResponseDTO.class))
                .collect(Collectors.toList());
    }

    // 병원 목록 조회 + 페이징
    @Override
    @Transactional(readOnly = true)
    public Page<HospitalResponseDTO> hospitalListPage(HospitalRequestDTO requestDTO, int page, String username) {

        resolveReferencePoint(requestDTO, username);

        Pageable pageable = PageRequest.of(page, 9);

        return hospitalRepository.searchHospitalsPage(requestDTO, pageable)
                .map(hospital -> modelMapper.map(hospital, HospitalResponseDTO.class));
    }

    // 거리순 정렬 기준 좌표 결정: 검색창에 입력한 지역이 있으면 그걸 쓰고,
    // 없으면 로그인한 회원의 저장된 주소를 기본값으로 사용한다.
    // 주소를 좌표로 변환할 수 없으면 거리순 정렬 없이 기존 목록을 그대로 보여준다.
    private void resolveReferencePoint(HospitalRequestDTO requestDTO, String username) {

        String referenceAddress = requestDTO.getRegion();

        if ((referenceAddress == null || referenceAddress.isBlank()) && username != null) {
            referenceAddress = memberRepository.findByUsername(username)
                    .map(Member::getAddress)
                    .orElse(null);
        }

        if (referenceAddress == null || referenceAddress.isBlank()) {
            return;
        }

        try {
            GeoPointDTO geoPoint = geocodingService.geocode(referenceAddress);
            requestDTO.setLatitude(geoPoint.getLatitude().doubleValue());
            requestDTO.setLongitude(geoPoint.getLongitude().doubleValue());
        } catch (RuntimeException e) {
            log.warn("근처 병원 검색 - 주소 좌표 변환 실패: {}", referenceAddress, e);
        }
    }

    // 병원 1개만 조회
    @Override
    @Transactional(readOnly = true)
    public HospitalResponseDTO hospitalReadOne(Long hospitalId) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("병원을 찾을 수 없습니다."));

        // 운영시간 변환
        List<HospitalHoursDTO> hoursDTOList = hospital.getHours().stream()
                .map(h -> HospitalHoursDTO.builder()
                        .dayOfWeek(h.getDayOfWeek())
                        .openTime(h.getOpenTime())
                        .closeTime(h.getCloseTime())
                        .isClosed(h.getIsClosed())
                        .build())
                .collect(Collectors.toList());

        // 이미지 URL 리스트
        List<String> imageUrls = hospital.getImages().stream()
                .map(img -> img.getImgUrl())
                .collect(Collectors.toList());

        return HospitalResponseDTO.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .latitude(hospital.getLatitude())
                .longitude(hospital.getLongitude())
                .phone(hospital.getPhone())
                .isEmergency(hospital.getIsEmergency())
                .department(hospital.getDepartment())
                .rating(hospital.getRating())
                .slotIntervalMin(hospital.getSlotIntervalMin())
                .maxPerSlot(hospital.getMaxPerSlot())
                .hours(hoursDTOList)
                .imageUrls(imageUrls)
                .build();
    }
}