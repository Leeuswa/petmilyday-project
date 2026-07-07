package com.petmilyday.repository.community;

import com.petmilyday.entity.community.MeetupComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetupCommentRepository extends JpaRepository<MeetupComment, Long> {
    @Query("SELECT mc FROM MeetupComment mc WHERE mc.meetupPost.id = :meetupPostId AND mc.parent IS NULL")
    Page<MeetupComment> listOfMeetupPost(@Param("meetupPostId") Long meetupPostId, Pageable pageable);
}