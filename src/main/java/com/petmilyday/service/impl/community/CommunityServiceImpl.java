package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.CommunityPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.CommunityPostRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.community.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public Long registerPost(String username, CommunityPostDTO dto) {
        Member member = memberRepository.findByUsername(username).orElseThrow();

        CommunityPost post = CommunityPost.builder()
                .member(member)
                .title(dto.getTitle())
                .content(dto.getContent())
                .anonymous(dto.isAnonymous())
                .build();

        return communityPostRepository.save(post).getId();
    }

    @Override
    public CommunityPostDTO readPost(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return convertToDTO(post);
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
    public PageResponseDTO<CommunityPostDTO> getList(PageRequestDTO pageRequestDTO) {
        Page<CommunityPost> result = communityPostRepository.searchAll(pageRequestDTO);

        List<CommunityPostDTO> dtoList = result.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<CommunityPostDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    // 특정 게시글 조회 수
    @Override
    public void updateViewCount(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.addViewCount();
    }

    // 내가 쓴 게시글
    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getMyPosts(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<CommunityPost> posts = communityPostRepository.findByMemberOrderByIdDesc(member);

        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 로그인 유저 관리자 판별
    private boolean isCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }

    // 엔티티를 DTO로 변환하면서 익명 처리 및 관리자 예외를 적용하는 메서드
    private CommunityPostDTO convertToDTO(CommunityPost post) {
        CommunityPostDTO dto = modelMapper.map(post, CommunityPostDTO.class);
        com.petmilyday.entity.member.Member writer = post.getMember();

        if (writer != null) {
            dto.setWriterUsername(writer.getUsername());

            String realName = (writer.getNickname() != null && !writer.getNickname().trim().isEmpty())
                    ? writer.getNickname() : writer.getName();

            if (post.isAnonymous()) {
                if (isCurrentAdmin()) {
                    dto.setWriterName("익명(" + realName + ")");
                } else {
                    dto.setWriterName("익명");
                }
            } else {
                dto.setWriterName(realName);
            }
        }
        return dto;
    }
}