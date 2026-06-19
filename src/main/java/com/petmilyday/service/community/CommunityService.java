package com.petmilyday.service.community;

import com.petmilyday.dto.community.CommunityPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;

import java.util.List;

public interface CommunityService {

    Long registerPost(String username, CommunityPostDTO dto);

    CommunityPostDTO readPost(Long id);

    void modifyPost(CommunityPostDTO dto);

    void removePost(Long id);

    PageResponseDTO<CommunityPostDTO> getList(PageRequestDTO pageRequestDTO);

    void updateViewCount(Long id);

    List<CommunityPostDTO> getMyPosts(String username);
}