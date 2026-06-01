package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.CommunityPostDTO;
import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.CommunityPostRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.community.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final MemberRepository memberRepository;

    @Override
    public Long registerPost(String username, CommunityPostDTO dto) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        // DTO의 텍스트 데이터와 조회한 Member 객체를 합쳐 엔티티 생성
        CommunityPost post = CommunityPost.builder()
                .member(member)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        return communityPostRepository.save(post).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPostDTO readPost(Long id) {

        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return CommunityPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writerName(post.getMember().getNickname())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Override
    public void modifyPost(CommunityPostDTO dto) {

        CommunityPost post = communityPostRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다."));

        post.updatePost(dto.getTitle(), dto.getContent());
    }

    @Override
    public void removePost(Long id) {
        communityPostRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunityPostDTO> getList(String[] types, String keyword, Pageable pageable) {

        Page<CommunityPost> result = communityPostRepository.searchAll(types, keyword, pageable);

        return result.map(post -> CommunityPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writerName(post.getMember().getNickname())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build()
        );
    }
}