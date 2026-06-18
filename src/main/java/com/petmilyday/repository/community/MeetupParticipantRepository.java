package com.petmilyday.repository.community;

import com.petmilyday.entity.community.MeetupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetupParticipantRepository extends JpaRepository<MeetupParticipant, Long> {

    // 특정 회원 모임 참여 여부
    boolean existsByMeetupPostIdAndMemberUsername(Long meetupPostId, String username);

    // 특정 회원 모임 참여 기록 조회
    Optional<MeetupParticipant> findByMeetupPostIdAndMemberUsername(Long meetupPostId, String username);

    // 모임 삭제 시 전체 회원 기록 삭제
    void deleteByMeetupPostId(Long meetupPostId);

    // 특정 회원 참여한 모임 목록 조회 (최신 날짜 기준)
    List<MeetupParticipant> findByMemberUsernameOrderByJoinedAtDesc(String username);
}