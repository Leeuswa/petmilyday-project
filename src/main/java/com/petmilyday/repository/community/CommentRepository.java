package com.petmilyday.repository.community;

import com.petmilyday.entity.community.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "select c from Comment c join fetch c.member where c.post.id = :postId",
            countQuery = "select count(c) from Comment c where c.post.id = :postId")
    Page<Comment> listOfPost(@Param("postId") Long postId, Pageable pageable);
}