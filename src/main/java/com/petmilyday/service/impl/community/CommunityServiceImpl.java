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
    @Transactional(readOnly = true)
    public CommunityPostDTO readPost(Long id) {
        CommunityPost post = communityPostRepository.findById(id).orElseThrow();

        String displayWriter = post.isAnonymous() ? "익명" : post.getMember().getDisplayName();

        return CommunityPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writerName(displayWriter) // "익명" 또는 "원래 닉네임"
                .writerUsername(post.getMember().getUsername())
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

    public PageResponseDTO<CommunityPostDTO> getList(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();

        boolean anonymousSearch = pageRequestDTO.isAnonymousSearch();
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<CommunityPost> result = communityPostRepository.searchAll(types, keyword, anonymousSearch, pageable);

        List<CommunityPostDTO> dtoList = result.getContent().stream()
                .map(post -> {
                    CommunityPostDTO dto = CommunityPostDTO.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .viewCount(post.getViewCount())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .anonymous(post.isAnonymous())
                            .build();

                    if (post.getMember() != null) {
                        if (post.isAnonymous()) {
                            dto.setWriterName("익명");
                        } else {
                            String writerName = (post.getMember().getNickname() != null && !post.getMember().getNickname().isEmpty())
                                    ? post.getMember().getNickname()
                                    : post.getMember().getName();
                            dto.setWriterName(writerName);
                        }
                        dto.setWriterUsername(post.getMember().getUsername());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<CommunityPostDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    public void updateViewCount(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.addViewCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getMyPosts(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<CommunityPost> posts = communityPostRepository.findByMemberOrderByIdDesc(member);

        return posts.stream()
                .map(post -> {
                    CommunityPostDTO dto = modelMapper.map(post, CommunityPostDTO.class);
                    if (post.isAnonymous()) {
                        dto.setWriterName("익명");
                    } else {
                        dto.setWriterName(member.getNickname() != null ? member.getNickname() : member.getName());
                    }
                    dto.setWriterUsername(member.getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}