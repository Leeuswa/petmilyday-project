package com.petmilyday.dto.admin;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDTO {

    private long hospitalCount;

    private long waitingHospitalManagerCount;

    private long reservationCount;

    private long waitingReservationCount;

    private long approvedReservationCount;

    private long cancelReservationCount;
}