package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.MeetupPostDTO;
import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.entity.community.MeetupParticipant;
import com.petmilyday.entity.community.MeetupPost;
import com.petmilyday.entity.community.MeetupStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.MeetupParticipantRepository;
import com.petmilyday.repository.community.MeetupPostRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.community.MeetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 모임 게시판 비즈니스 로직 구현
@Service
@RequiredArgsConstructor
@Transactional
public class MeetupServiceImpl implements MeetupService {

    private final MeetupPostRepository meetupPostRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Long registerMeetup(String username, MeetupPostDTO dto) {
        Member host = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        MeetupPost meetupPost = MeetupPost.builder()
                .host(host)
                .title(dto.getTitle())
                .content(dto.getContent())
                .location(dto.getLocation())
                .meetupDate(dto.getMeetupDate())
                .maxParticipants(dto.getMaxParticipants())
                .currentParticipants(1)
                .status(com.petmilyday.entity.community.MeetupStatus.RECRUITING)
                .build();

        meetupPostRepository.save(meetupPost);

        // 추가 가입 내역 기록 연동
        MeetupParticipant participant = MeetupParticipant.builder()
                .meetupPost(meetupPost)
                .member(host)
                .status("APPROVED")
                .build();
        meetupParticipantRepository.save(participant);

        return meetupPost.getId();
    }

    // 목록 조회 시 마감 상태 실시간 검증 반영
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<MeetupPostDTO> getList(PageRequestDTO pageRequestDTO) {

        org.springframework.data.domain.Pageable pageable = pageRequestDTO.getPageable("id");

        org.springframework.data.domain.Page<MeetupPost> result = meetupPostRepository.findAllWithHost(pageable);

        java.util.List<MeetupPostDTO> dtoList = result.getContent().stream()
                .map(post -> {
                    MeetupPostDTO dto = MeetupPostDTO.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .location(post.getLocation())
                            .meetupDate(post.getMeetupDate())
                            .maxParticipants(post.getMaxParticipants())
                            .currentParticipants(post.getCurrentParticipants())
                            .status(post.getStatus().name())
                            .viewCount(post.getViewCount())
                            .createdAt(post.getCreatedAt())
                            .build();

                    if (post.getHost() != null) {
                        String hostName = (post.getHost().getNickname() != null && !post.getHost().getNickname().isEmpty())
                                ? post.getHost().getNickname()
                                : post.getHost().getName();
                        dto.setHostName(hostName);
                        dto.setHostUsername(post.getHost().getUsername());
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());

        return PageResponseDTO.<MeetupPostDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MeetupPostDTO read(Long id, String username) {
        MeetupPost post = meetupPostRepository.findById(id).orElseThrow();

        // 현재 접속자의 참여 여부 확인
        boolean isParticipating = meetupParticipantRepository.existsByMeetupPostIdAndMemberUsername(id, username);

        return MeetupPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .location(post.getLocation())
                .meetupDate(post.getMeetupDate())
                .currentParticipants(post.getCurrentParticipants())
                .maxParticipants(post.getMaxParticipants())
                .status(post.getStatus().name())
                .hostName(post.getHost().getNickname() != null ? post.getHost().getNickname() : post.getHost().getName())
                .hostUsername(post.getHost().getUsername())
                .viewCount(post.getViewCount())
                .isParticipating(isParticipating)
                .build();

    }

    // 참여 신청 로직
    @Override
    public Long registerParticipant(Long meetupId, String username) {
        MeetupPost post = meetupPostRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (meetupParticipantRepository.existsByMeetupPostIdAndMemberUsername(meetupId, username)) {
            throw new IllegalStateException("이미 신청한 모임입니다.");
        }

        MeetupParticipant participant = MeetupParticipant.builder()
                .meetupPost(post)
                .member(member)
                .status("PENDING")
                .build();

        meetupParticipantRepository.save(participant);
        return participant.getId();
    }

    // 신청자 조회
    @Override
    @Transactional(readOnly = true)
    public List<MeetupParticipant> getApplicants(Long meetupId, String hostUsername) {
        MeetupPost post = meetupPostRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));

        if (!post.getHost().getUsername().equals(hostUsername)) {
            throw new IllegalStateException("조회 권한이 없습니다.");
        }

        List<MeetupParticipant> list = meetupParticipantRepository.findByMeetupPostIdOrderByJoinedAtAsc(meetupId);

        list.forEach(participant -> {
            participant.getMember().getPetProfiles().size();
        });

        return list;
    }

    // 신청자 수락 로직
    @Override
    @Transactional
    public void approveParticipant(Long id) {
        MeetupParticipant participant = meetupParticipantRepository.findById(id).orElseThrow();

        participant.approve();
        meetupParticipantRepository.save(participant);

        MeetupPost post = participant.getMeetupPost();
        post.addParticipant();
        meetupPostRepository.save(post);
    }

    // 신청자 거절 로직
    @Override
    @Transactional
    public void rejectParticipant(Long id) {
        MeetupParticipant participant = meetupParticipantRepository.findById(id).orElseThrow();

        meetupParticipantRepository.delete(participant);
    }

    @Override
    @Transactional
    public void cancelMeetup(Long meetupPostId, String username) {
        MeetupParticipant participant = meetupParticipantRepository.findByMeetupPostIdAndMemberUsername(meetupPostId, username)
                .orElseThrow(() -> new IllegalArgumentException("참여 기록이 없습니다."));

        MeetupPost post = participant.getMeetupPost();

        // 원래 승인이였는지 확인 후, 인원 감소
        if ("APPROVED".equals(participant.getStatus())) {
            post.removeParticipant();
        }

        meetupParticipantRepository.delete(participant);
    }

    @Override
    public void deleteMeetup(Long id, String username) {
        MeetupPost post = meetupPostRepository.findById(id).orElseThrow();

        if (!post.getHost().getUsername().equals(username)) {
            throw new IllegalStateException("모임 삭제 권한이 없습니다.");
        }

        meetupParticipantRepository.deleteByMeetupPostId(id);
        meetupPostRepository.delete(post);
    }

    @Override
    public void modifyMeetup(String username, MeetupPostDTO dto) {
        MeetupPost post = meetupPostRepository.findById(dto.getId()).orElseThrow();

        // 작성자 본인 확인 검증
        if (!post.getHost().getUsername().equals(username)) {
            throw new IllegalStateException("모임 수정 권한이 없습니다.");
        }

        // 엔티티 데이터 변경 수행
        post.updateMeetup(dto.getTitle(), dto.getContent(), dto.getLocation(), dto.getMeetupDate(), dto.getMaxParticipants());
    }

    // 조회수 증가 로직 구현
    @Override
    public void updateViewCount(Long id) {
        MeetupPost post = meetupPostRepository.findById(id).orElseThrow();
        post.addViewCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetupPostDTO> getMyMeetups(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<MeetupPost> posts = meetupPostRepository.findByHostOrderByIdDesc(member);

        return posts.stream()
                .map(post -> MeetupPostDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .location(post.getLocation())
                        .meetupDate(post.getMeetupDate())
                        .maxParticipants(post.getMaxParticipants())
                        .currentParticipants(post.getCurrentParticipants())
                        .status(post.getStatus() != null ? post.getStatus().name() : "RECRUITING")
                        .viewCount(post.getViewCount())
                        .createdAt(post.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetupPostDTO> getParticipatedMeetups(String username) {
        List<MeetupParticipant> participants = meetupParticipantRepository.findByMemberUsernameOrderByJoinedAtDesc(username);

        return participants.stream()
                .filter(participant -> {
                    try {
                        participant.getMeetupPost().getTitle();
                        return true;
                    } catch (jakarta.persistence.EntityNotFoundException e) {
                        return false;
                    }
                })
                .map(participant -> {
                    MeetupPost post = participant.getMeetupPost();
                    return MeetupPostDTO.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .location(post.getLocation())
                            .meetupDate(post.getMeetupDate())
                            .maxParticipants(post.getMaxParticipants())
                            .currentParticipants(post.getCurrentParticipants())
                            .status(post.getStatus() != null ? post.getStatus().name() : "RECRUITING")
                            .hostName(post.getHost() != null ? (post.getHost().getNickname() != null ? post.getHost().getNickname() : post.getHost().getName()) : "알수없음")
                            .hostUsername(post.getHost() != null ? post.getHost().getUsername() : "")
                            .viewCount(post.getViewCount())
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}