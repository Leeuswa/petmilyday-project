package com.petmilyday.service.community;

import com.petmilyday.dto.community.CommunityPostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityService {

    Long registerPost(String username, CommunityPostDTO dto);

    CommunityPostDTO readPost(Long id);

    void modifyPost(CommunityPostDTO dto);

    void removePost(Long id);

    Page<CommunityPostDTO> getList(String[] types, String keyword, Pageable pageable);
}