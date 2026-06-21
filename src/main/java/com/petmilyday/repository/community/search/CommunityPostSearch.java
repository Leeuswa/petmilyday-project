package com.petmilyday.repository.community.search;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.entity.community.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityPostSearch {

    Page<CommunityPost> searchAll(PageRequestDTO pageRequestDTO);
}