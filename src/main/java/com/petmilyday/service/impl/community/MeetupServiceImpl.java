package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.MeetupPostDTO;
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
    public Long registerMeetup(String username, MeetupPostDTO dto) {
        Member host = memberRepository.findByUsername(username).orElseThrow();

        MeetupPost post = MeetupPost.builder()
                .host(host)
                .title(dto.getTitle())
                .content(dto.getContent())
                .location(dto.getLocation())
                .meetupDate(dto.getMeetupDate())
                .maxParticipants(dto.getMaxParticipants())
                .build();

        MeetupPost savedPost = meetupPostRepository.save(post);

        // 방장 자동 참여 처리
        MeetupParticipant participant = MeetupParticipant.builder()
                .meetupPost(savedPost)
                .member(host)
                .build();
        meetupParticipantRepository.save(participant);

        return savedPost.getId();
    }

    // 목록 조회 시 마감 상태 실시간 검증 반영
    @Override
    @Transactional(readOnly = true)
    public List<MeetupPostDTO> getList() {
        return meetupPostRepository.findAll().stream()
                .map(post -> {
                    // 인원 충족 시 마감 상태 강제 적용
                    String calculatedStatus = post.getCurrentParticipants() >= post.getMaxParticipants()
                            ? MeetupStatus.CLOSED.name()
                            : post.getStatus().name();

                    return MeetupPostDTO.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .location(post.getLocation())
                            .meetupDate(post.getMeetupDate())
                            .currentParticipants(post.getCurrentParticipants())
                            .maxParticipants(post.getMaxParticipants())
                            .status(calculatedStatus)
                            .hostName(post.getHost().getNickname() != null ? post.getHost().getNickname() : post.getHost().getName())
                            .viewCount(post.getViewCount())
                            .build();
                })
                .collect(Collectors.toList());
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

    @Override
    public void joinMeetup(Long id, String username) {
        MeetupPost post = meetupPostRepository.findById(id).orElseThrow();
        Member member = memberRepository.findByUsername(username).orElseThrow();

        if (meetupParticipantRepository.existsByMeetupPostIdAndMemberUsername(id, username)) {
            throw new IllegalStateException("이미 참여 중인 모임입니다.");
        }

        // 인원 증가 및 마감 상태 변경
        post.addParticipant();

        MeetupParticipant participant = MeetupParticipant.builder()
                .meetupPost(post)
                .member(member)
                .build();
        meetupParticipantRepository.save(participant);
    }

    @Override
    public void cancelMeetup(Long id, String username) {
        MeetupParticipant participant = meetupParticipantRepository.findByMeetupPostIdAndMemberUsername(id, username)
                .orElseThrow(() -> new IllegalStateException("참여 기록이 없습니다."));

        MeetupPost post = participant.getMeetupPost();

        // 방장 취소 방지
        if (post.getHost().getUsername().equals(username)) {
            throw new IllegalStateException("방장은 참여를 취소할 수 없습니다. 모임을 삭제해 주세요.");
        }

        // 인원 감소 및 상태 롤백 로직 (엔티티에 메서드 필요)
        post.removeParticipant();
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
}