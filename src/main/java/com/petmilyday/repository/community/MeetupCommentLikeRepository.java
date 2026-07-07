package com.petmilyday.repository.community;

import com.petmilyday.entity.community.MeetupCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MeetupCommentLikeRepository extends JpaRepository<MeetupCommentLike, Long> {
    boolean existsByMemberUsernameAndCommentId(String username, Long commentId);
    Optional<MeetupCommentLike> findByMemberUsernameAndCommentId(String username, Long commentId);
    int countByCommentId(Long commentId);
}