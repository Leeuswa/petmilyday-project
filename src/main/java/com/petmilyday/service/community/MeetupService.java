package com.petmilyday.service.community;

import com.petmilyday.dto.community.CommunityPostDTO;
import com.petmilyday.dto.community.MeetupPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.entity.community.MeetupParticipant;

import java.util.List;

public interface MeetupService {

    Long registerMeetup(String username, MeetupPostDTO dto);

    PageResponseDTO<MeetupPostDTO> getList(PageRequestDTO pageRequestDTO);

    // 모임 상세 조회
    MeetupPostDTO read(Long id, String username);

    // 모임 참여 취소
    void cancelMeetup(Long id, String username);

    void deleteMeetup(Long id, String username);

    void modifyMeetup(String username, MeetupPostDTO dto);

    void updateViewCount(Long id);

    List<MeetupPostDTO> getMyMeetups(String username);

    List<MeetupPostDTO> getParticipatedMeetups(String username);

    // 참여 신청 (상태 PENDING으로 생성)
    Long registerParticipant(Long meetupId, String username);

    // 신청자 명단 조회
    List<MeetupParticipant> getApplicants(Long meetupId, String hostUsername);

    // 신청자 수락
    void approveParticipant(Long Id);
    // 신청자 거절
    void rejectParticipant(Long Id);
}