package com.petmilyday.dto.hospital;

import com.petmilyday.entity.hospital.HospitalHours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HospitalResponseDTO {

    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private Boolean isEmergency;
    private String department;
    private Double rating;
    private Integer slotIntervalMin;
    private Integer maxPerSlot;


    //운영시간, 이미지는 상세페이지에서만 적용
    private List<HospitalHoursDTO> hours;
    private List<String> imageUrls;

}
