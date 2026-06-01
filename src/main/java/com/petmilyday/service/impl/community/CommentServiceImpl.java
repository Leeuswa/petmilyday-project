package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.dto.community.CommentDTO;
import com.petmilyday.entity.community.Comment;
import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.member.Member;
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
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public Long register(String username, CommentDTO commentDTO) {
        Member member = memberRepository.findByUsername(username).orElseThrow();
        CommunityPost post = communityPostRepository.findById(commentDTO.getPostId()).orElseThrow();

        Comment comment = modelMapper.map(commentDTO, Comment.class);
        comment.assignMemberAndPost(member, post);

        return commentRepository.save(comment).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO read(Long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        Comment comment = commentOptional.orElseThrow();
        CommentDTO dto = modelMapper.map(comment, CommentDTO.class);
        dto.setWriterName(comment.getMember().getNickname());
        return dto;
    }

    @Override
    public void modify(CommentDTO commentDTO) {
        Optional<Comment> commentOptional = commentRepository.findById(commentDTO.getId());
        Comment comment = commentOptional.orElseThrow();
        comment.updateContent(commentDTO.getContent());
        commentRepository.save(comment);
    }

    @Override
    public void remove(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow();
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

        List<CommentDTO> dtoList = result.getContent().stream()
                .map(comment -> {
                    CommentDTO dto = modelMapper.map(comment, CommentDTO.class);
                    dto.setWriterName(comment.getMember().getDisplayName());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<CommentDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }
}