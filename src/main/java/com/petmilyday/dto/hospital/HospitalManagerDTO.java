package com.petmilyday.dto.hospital;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalManagerDTO {

    private Long hospitalId;

    private String managerName;

    private String managerPhone;

    private String businessNumber;
}