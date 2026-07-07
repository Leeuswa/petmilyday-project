package com.petmilyday.repository.community;

import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.search.CommunityPostSearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long>, CommunityPostSearch {
    List<CommunityPost> findByMemberOrderByIdDesc(Member member);
}