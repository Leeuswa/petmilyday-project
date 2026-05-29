package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.hospital.HospitalReviewRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.hospital.HospitalReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalReviewServiceImpl implements HospitalReviewService {
    private final HospitalReviewRepository hospitalReviewRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    @Override
    public void reviewRegister(HospitalReviewRequestDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
        if(!reservation.getStatus().equals("DONE")){
            throw new RuntimeException("진료 완료 후에만 리뷰를 작성할 수 있습니다.");
        }
        boolean exists =
                hospitalReviewRepository.existsByReservation(reservation);

        if(exists){
            throw new RuntimeException("이미 리뷰를 작성한 예약입니다.");
        }

        HospitalReview review = HospitalReview.builder()
                .member(reservation.getMember())
                .hospital(reservation.getHospital())
                .reservation(reservation)
                .rating(dto.getRating())
                .content(dto.getContent())
                .build();



        hospitalReviewRepository.save(review);
    }

    @Override
    public List<HospitalReviewResponseDTO> reviewList(Long hospitalId) {
        List<HospitalReview> reviews = hospitalReviewRepository.
                findByHospitalIdAndIsReportedFalseOrderByCreatedAtDesc(hospitalId);

        return reviews.stream()
                .map(review -> {
                    HospitalReviewResponseDTO dto = modelMapper.map(review, HospitalReviewResponseDTO.class);
                    dto.setMemberNickname(review.getMember().getNickname());
                    dto.setMemberId(review.getMember().getId());
                    dto.setHospitalId(review.getHospital().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //리뷰 수정
    @Transactional
    @Override
    public void reviewModify(Long reviewId, HospitalReviewRequestDTO dto) {
        HospitalReview review = hospitalReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("작성된 리뷰가 없습니다."));

        review.contentChange(dto.getContent(), dto.getRating());

//        hospitalReviewRepository.save(review);

    }

    //리뷰삭제
    @Transactional
    @Override
    public void reviewRemove(Long reviewId) {
        HospitalReview review = hospitalReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("작성된 리뷰가 없습니다."));
        hospitalReviewRepository.deleteById(reviewId);

    }
}
