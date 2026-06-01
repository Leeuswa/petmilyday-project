package com.petmilyday.repository.community;

import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.repository.community.search.CommunityPostSearch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long>, CommunityPostSearch {
}