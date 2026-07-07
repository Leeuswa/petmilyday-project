package com.petmilyday.dto.geocoding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeoPointDTO {

    private BigDecimal latitude;
    private BigDecimal longitude;
}
