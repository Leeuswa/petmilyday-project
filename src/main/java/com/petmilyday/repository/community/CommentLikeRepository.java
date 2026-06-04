package com.petmilyday.repository.community;

import com.petmilyday.entity.community.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByMemberUsernameAndCommentId(String username, Long commentId);

    int countByCommentId(Long commentId);

    boolean existsByMemberUsernameAndCommentId(String username, Long commentId);
}