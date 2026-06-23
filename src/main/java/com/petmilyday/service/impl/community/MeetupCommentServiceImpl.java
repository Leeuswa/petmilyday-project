package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.MeetupCommentDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.entity.community.MeetupComment;
import com.petmilyday.entity.community.MeetupCommentLike;
import com.petmilyday.entity.community.MeetupPost;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.MeetupCommentLikeRepository;
import com.petmilyday.repository.community.MeetupCommentRepository;
import com.petmilyday.repository.community.MeetupPostRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.community.MeetupCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MeetupCommentServiceImpl implements MeetupCommentService {

    private final MeetupCommentRepository meetupCommentRepository;
    private final MeetupCommentLikeRepository meetupCommentLikeRepository;
    private final MeetupPostRepository meetupPostRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public Long register(String username, MeetupCommentDTO meetupCommentDTO) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        MeetupPost meetupPost = meetupPostRepository.findById(meetupCommentDTO.getMeetupPostId())
                .orElseThrow(() -> new IllegalArgumentException("해당 모임 게시글을 찾을 수 없습니다."));

        MeetupComment parent = null;
        if (meetupCommentDTO.getParentId() != null) {
            parent = meetupCommentRepository.findById(meetupCommentDTO.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }

        MeetupComment meetupComment = MeetupComment.builder()
                .member(member)
                .meetupPost(meetupPost)
                .content(meetupCommentDTO.getContent())
                .parent(parent)
                .build();

        return meetupCommentRepository.save(meetupComment).getId();
    }

    @Override
    public MeetupCommentDTO read(Long id) {
        MeetupComment meetupComment = meetupCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        return convertToDTO(meetupComment);
    }

    @Override
    public void modify(MeetupCommentDTO meetupCommentDTO) {
        MeetupComment meetupComment = meetupCommentRepository.findById(meetupCommentDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        meetupComment.changeContent(meetupCommentDTO.getContent());
    }

    @Override
    public void remove(Long id) {
        meetupCommentRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<MeetupCommentDTO> getListOfMeetupPost(Long meetupPostId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() <= 0 ? 0 : pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("id").ascending()
        );

        // 1. 부모 댓글들 페이징 조회
        Page<MeetupComment> result = meetupCommentRepository.listOfMeetupPost(meetupPostId, pageable);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : null;

        // 2. 부모 댓글과 자식 대댓글들을 순서대로 하나의 리스트로 평탄화(Flat)하여 반환
        List<MeetupCommentDTO> dtoList = result.getContent().stream()
                .flatMap(parentComment -> {
                    List<MeetupCommentDTO> flatList = new ArrayList<>();

                    // 부모 댓글 처리
                    MeetupCommentDTO parentDTO = convertToDTO(parentComment);
                    setLikeInfo(parentDTO, parentComment, currentUsername);
                    flatList.add(parentDTO);

                    // 자식 대댓글 처리 (있다면 순서대로 하단에 추가)
                    if (parentComment.getChildren() != null) {
                        for (MeetupComment child : parentComment.getChildren()) {
                            MeetupCommentDTO childDTO = convertToDTO(child);
                            setLikeInfo(childDTO, child, currentUsername);
                            flatList.add(childDTO);
                        }
                    }
                    return flatList.stream();
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<MeetupCommentDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    public int toggleLike(String username, Long commentId) {
        Optional<MeetupCommentLike> alreadyLike =
                meetupCommentLikeRepository.findByMemberUsernameAndCommentId(username, commentId);

        if (alreadyLike.isPresent()) {
            meetupCommentLikeRepository.delete(alreadyLike.get());
        } else {
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
            MeetupComment meetupComment = meetupCommentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

            MeetupCommentLike meetupCommentLike = MeetupCommentLike.builder()
                    .member(member)
                    .comment(meetupComment)
                    .build();
            meetupCommentLikeRepository.save(meetupCommentLike);
        }

        return meetupCommentLikeRepository.countByCommentId(commentId);
    }

    // 엔티티 -> DTO 공통 변환 메서드
    private MeetupCommentDTO convertToDTO(MeetupComment meetupComment) {
        MeetupCommentDTO dto = modelMapper.map(meetupComment, MeetupCommentDTO.class);
        Member writer = meetupComment.getMember();
        if (writer != null) {
            dto.setWriterUsername(writer.getUsername());
            dto.setWriterName((writer.getNickname() != null && !writer.getNickname().trim().isEmpty())
                    ? writer.getNickname() : writer.getName());
        }
        if (meetupComment.getParent() != null) {
            dto.setParentId(meetupComment.getParent().getId());
        }
        return dto;
    }

    // 좋아요 정보 세팅 공통 메서드
    private void setLikeInfo(MeetupCommentDTO dto, MeetupComment comment, String currentUsername) {
        int likeCount = meetupCommentLikeRepository.countByCommentId(comment.getId());
        dto.setLikeCount(likeCount);

        if (currentUsername != null && !currentUsername.equals("anonymousUser")) {
            boolean isLiked = meetupCommentLikeRepository.existsByMemberUsernameAndCommentId(currentUsername, comment.getId());
            dto.setLikedByCurrentUser(isLiked);
        } else {
            dto.setLikedByCurrentUser(false);
        }
    }
}