package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.dto.community.CommentDTO;
import com.petmilyday.entity.community.Comment;
import com.petmilyday.entity.community.CommentLike;
import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.CommentLikeRepository;
import com.petmilyday.repository.community.CommentRepository;
import com.petmilyday.repository.community.CommunityPostRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.community.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public Long register(String username, CommentDTO commentDTO) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
        CommunityPost post = communityPostRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(commentDTO.getContent())
                .member(member)
                .post(post)
                .build();

        // 대댓글 작성시 부모 댓글을 찾아주는 로직
        if (commentDTO.getParentId() != null) {
            Comment parent = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            comment.assignParent(parent);
        }

        return commentRepository.save(comment).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO read(Long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        Comment comment = commentOptional.orElseThrow();
        CommentDTO dto = modelMapper.map(comment, CommentDTO.class);
        dto.setWriterName(comment.getMember().getNickname());
        dto.setWriterUsername(comment.getMember().getUsername());
        return dto;
    }

    @Override
    public void modify(CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(commentDTO.getId()).orElseThrow();

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getMember().getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        comment.updateContent(commentDTO.getContent());
        commentRepository.save(comment);
    }

    @Override
    public void remove(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow();

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getMember().getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        commentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CommentDTO> getListOfPost(Long postId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() <= 0 ? 0 : pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("id").ascending()
        );

        Page<Comment> result = commentRepository.listOfPost(postId, pageable);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        List<CommentDTO> dtoList = result.getContent().stream()
                .map(comment -> {
                    CommentDTO dto = modelMapper.map(comment, CommentDTO.class);
                    dto.setWriterName(comment.getMember().getDisplayName());
                    dto.setWriterUsername(comment.getMember().getUsername());

                    if (comment.getParent() != null) {
                        dto.setParentId(comment.getParent().getId());
                    }
                    dto.setLikeCount(commentLikeRepository.countByCommentId(comment.getId()));

                    if (!"anonymousUser".equals(currentUsername)) {
                        boolean isLiked = commentLikeRepository.existsByMemberUsernameAndCommentId(currentUsername, comment.getId());
                        dto.setLikedByCurrentUser(isLiked);
                    } else {
                        dto.setLikedByCurrentUser(false); // 비로그인은 빈 하트
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<CommentDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    public int toggleLike(String username, Long commentId) {
        // 좋아요가 눌려져 있는 상태인지 확인
        java.util.Optional<CommentLike> alreadyLike =
                commentLikeRepository.findByMemberUsernameAndCommentId(username, commentId);

        if (alreadyLike.isPresent()) {
            commentLikeRepository.delete(alreadyLike.get());
        } else {
            Member member = memberRepository.findByUsername(username).orElseThrow();
            Comment comment = commentRepository.findById(commentId).orElseThrow();

            CommentLike commentLike = CommentLike.builder()
                    .member(member)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(commentLike);
        }

        return commentLikeRepository.countByCommentId(commentId);
    }
}