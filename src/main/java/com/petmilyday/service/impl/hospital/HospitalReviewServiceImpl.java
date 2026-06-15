package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.hospital.HospitalReviewRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.hospital.HospitalReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalReviewServiceImpl implements HospitalReviewService {

    private final HospitalReviewRepository hospitalReviewRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    // 리뷰 등록
    @Override
    @Transactional
    public void reviewRegister(HospitalReviewRequestDTO dto, String username) {

        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        // 본인 인증
        if (!reservation.getMember().getUsername().equals(username)) {
            throw new RuntimeException("본인 예약만 리뷰를 작성할 수 있습니다.");
        }

        // 권한 확인
        if (reservation.getStatus() != ReservationStatus.DONE) {
            throw new RuntimeException("진료 완료 후에만 리뷰를 작성할 수 있습니다.");
        }

        // 리뷰 존재 여부 확인
        boolean exists = hospitalReviewRepository.existsByReservation(reservation);

        if (exists) {
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

    // 리뷰 리스트
    @Override
    @Transactional(readOnly = true)
    public List<HospitalReviewResponseDTO> reviewList(Long hospitalId) {

        List<HospitalReview> reviews =
                hospitalReviewRepository.findByHospitalIdAndIsReportedFalseOrderByCreatedAtDesc(hospitalId);

        return reviews.stream()
                .map(review -> {
                    HospitalReviewResponseDTO dto =
                            modelMapper.map(review, HospitalReviewResponseDTO.class);

                    dto.setMemberNickname(review.getMember().getNickname());
                    dto.setMemberId(review.getMember().getId());
                    dto.setHospitalId(review.getHospital().getId());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    @Override
    @Transactional
    public void reviewModify(Long reviewId, HospitalReviewRequestDTO dto, String username) {

        HospitalReview review = hospitalReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("작성된 리뷰가 없습니다."));

        if (!review.getMember().getUsername().equals(username)) {
            throw new RuntimeException("본인 리뷰만이 수정이 가능합니다.");
        }

        review.contentChange(dto.getContent(), dto.getRating());
    }

    // 리뷰 삭제
    @Override
    @Transactional
    public void reviewRemove(Long reviewId, String username) {

        HospitalReview review = hospitalReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("작성된 리뷰가 없습니다."));

        if (!review.getMember().getUsername().equals(username)) {
            throw new RuntimeException("본인 리뷰만이 삭제가 가능합니다.");
        }

        hospitalReviewRepository.deleteById(reviewId);
    }

    // 리뷰 작성 가능한 예약 ID 찾기
    @Override
    @Transactional(readOnly = true)
    public Long findReviewableReservationId(Long hospitalId, String username) {

        List<Reservation> reservations = reservationRepository.findReviewableReservations(
                hospitalId,
                username,
                ReservationStatus.DONE
        );

        for (Reservation reservation : reservations) {

            boolean alreadyByReviewed =
                    hospitalReviewRepository.existsByReservation(reservation);

            if (!alreadyByReviewed) {
                return reservation.getId();
            }
        }

        return null;
    }

    // 내 리뷰 목록
    @Override
    @Transactional(readOnly = true)
    public List<HospitalReviewResponseDTO> myReivewList(String username) {

        List<HospitalReview> reviewList = hospitalReviewRepository.findMyReviews(username);

        return reviewList.stream()
                .map(review -> {
                    HospitalReviewResponseDTO dto =
                            modelMapper.map(review, HospitalReviewResponseDTO.class);

                    dto.setMemberNickname(review.getMember().getNickname());
                    dto.setMemberId(review.getMember().getId());
                    dto.setHospitalId(review.getHospital().getId());
                    dto.setHospitalName(review.getHospital().getName());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}