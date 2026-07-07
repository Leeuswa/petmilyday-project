package com.petmilyday.service.community;

import com.petmilyday.dto.community.MeetupCommentDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;

public interface MeetupCommentService {

    Long register(String username, MeetupCommentDTO meetupCommentDTO);

    MeetupCommentDTO read(Long id);

    void modify(MeetupCommentDTO meetupCommentDTO);

    void remove(Long id);

    PageResponseDTO<MeetupCommentDTO> getListOfMeetupPost(Long meetupPostId, PageRequestDTO pageRequestDTO);

    int toggleLike(String username, Long commentId);
}