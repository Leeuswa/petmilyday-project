package com.petmilyday.service.community;

import com.petmilyday.dto.community.MeetupPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;

import java.util.List;

public interface MeetupService {

    Long registerMeetup(String username, MeetupPostDTO dto);

    PageResponseDTO<MeetupPostDTO> getList(PageRequestDTO pageRequestDTO);

    // 모임 상세 조회
    MeetupPostDTO read(Long id, String username);

    // 모임 참여
    void joinMeetup(Long id, String username);

    // 모임 참여 취소
    void cancelMeetup(Long id, String username);

    void deleteMeetup(Long id, String username);

    void modifyMeetup(String username, MeetupPostDTO dto);

    void updateViewCount(Long id);
}