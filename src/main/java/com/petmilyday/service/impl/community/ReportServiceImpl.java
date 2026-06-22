package com.petmilyday.service.impl.community;

import com.petmilyday.dto.community.ReportDTO;
import com.petmilyday.dto.notification.NotificationDTO;
import com.petmilyday.entity.community.*;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.community.*;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommentRepository commentRepository;
    private final MeetupPostRepository meetupPostRepository;
    private final MeetupCommentRepository meetupCommentRepository;
    private final NotificationService notificationService;

    // 신고하기 등록
    public Long registerReport(String username, ReportDTO.Request dto) {
        Member reporter = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Report report = Report.builder()
                .reporter(reporter)
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .reason(dto.getReason())
                .build();

        return reportRepository.save(report).getId();
    }

    // 전체 신고 목록 조회 (관리자)
    @Transactional(readOnly = true)
    public List<ReportDTO.Response> getReportList() {
        return reportRepository.findAllByOrderByStatusAscIdDesc().stream().map(report -> {
            String summary = "존재하지 않는 글/댓글";
            try {
                if ("POST".equals(report.getTargetType())) {
                    summary = "[게시글] " + communityPostRepository.findById(report.getTargetId()).orElseThrow().getTitle();
                } else if ("COMMENT".equals(report.getTargetType())) {
                    summary = "[댓글] " + commentRepository.findById(report.getTargetId()).orElseThrow().getContent();
                }
            } catch (Exception e) {
                summary = "이미 삭제된 항목입니다.";
            }

            return ReportDTO.Response.builder()
                    .id(report.getId())
                    .reporterUsername(report.getReporter().getUsername())
                    .targetType(report.getTargetType())
                    .targetId(report.getTargetId())
                    .reason(report.getReason())
                    .status(report.getStatus())
                    .createdAt(report.getCreatedAt())
                    .targetContentSummary(summary)
                    .build();
        }).collect(Collectors.toList());
    }

    // 신고 글 삭제 처리 및 알림 발송
    public void processDelete(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        Member targetUser = null;
        String contentTitle = "";


        if ("POST".equals(report.getTargetType())) { // 일반 게시글
            CommunityPost post = communityPostRepository.findById(report.getTargetId()).orElse(null);
            if (post != null) {
                targetUser = post.getMember();
                contentTitle = post.getTitle();

                communityPostRepository.delete(post);
            }
        } else if ("COMMENT".equals(report.getTargetType())) { // 일반 댓글
            Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
            if (comment != null) {
                targetUser = comment.getMember();
                contentTitle = comment.getContent();

                commentRepository.delete(comment);
            }
        } else if ("MEETUP_POST".equals(report.getTargetType())) { // 모임 게시글
            MeetupPost meetupPost = meetupPostRepository.findById(report.getTargetId()).orElse(null);
            if (meetupPost != null) {
                targetUser = meetupPost.getHost(); // 모임 방장
                contentTitle = meetupPost.getTitle();

                meetupPostRepository.delete(meetupPost);
            }
        } else if ("MEETUP_COMMENT".equals(report.getTargetType())) { // 모임 댓글
            MeetupComment meetupComment = meetupCommentRepository.findById(report.getTargetId()).orElse(null);
            if (meetupComment != null) {
                targetUser = meetupComment.getMember();
                contentTitle = meetupComment.getContent();

                meetupCommentRepository.delete(meetupComment);
            }
        }

        report.updateStatusToDeleted(); // 신고 상태를 DELETED로 변경

        // 피신고자(작성자)에게 SSE 알림 전송
        if (targetUser != null) {
            String message = String.format("[%s] 항목이 '%s' 사유로 신고되어 삭제 처리되었습니다.",
                    contentTitle, report.getReason());

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .message(message)
                    .url("list")
                    .build();

            notificationService.sendToUser(targetUser.getUsername(), notificationDTO);
        }
    }

    // 신고 글 유지 처리 및 알림 발송
    public void processMaintain(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));

        Member targetUser = null;
        String contentTitle = "";
        String url = ""; // 알림 클릭 시 이동할 경로

        if ("POST".equals(report.getTargetType())) {
            CommunityPost post = communityPostRepository.findById(report.getTargetId()).orElse(null);
            if (post != null) {
                targetUser = post.getMember();
                contentTitle = post.getTitle();
            }
        } else if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
            if (comment != null) {
                targetUser = comment.getMember();
                contentTitle = comment.getContent();
            }
        } else if ("MEETUP_POST".equals(report.getTargetType())) { // 모임 게시글
            MeetupPost meetupPost = meetupPostRepository.findById(report.getTargetId()).orElse(null);
            if (meetupPost != null) {
                targetUser = meetupPost.getHost(); // 모임 방장
                contentTitle = meetupPost.getTitle();
                url = "/community/meetup/read?id=" + report.getTargetId();
            }
        } else if ("MEETUP_COMMENT".equals(report.getTargetType())) { // 모임 댓글
            MeetupComment meetupComment = meetupCommentRepository.findById(report.getTargetId()).orElse(null);
            if (meetupComment != null) {
                targetUser = meetupComment.getMember();
                contentTitle = meetupComment.getContent();
                url = "/community/meetup/read?id=" + meetupComment.getMeetupPost().getId();
            }
        }

        report.updateStatusToMaintained(); // 신고 상태를 MAINTAINED로 변경

        // 피신고자(작성자)에게 SSE 알림 전송
        if (targetUser != null) {
            String message = String.format("[%s] 항목에 대한 신고가 검토 결과 문제 없음으로 판명되어 유지 처리되었습니다.",
                    contentTitle);

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .message(message)
                    .url(url)
                    .build();

            notificationService.sendToUser(targetUser.getUsername(), notificationDTO);
        }
    }
}