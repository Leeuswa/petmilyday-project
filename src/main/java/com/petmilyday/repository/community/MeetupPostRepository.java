package com.petmilyday.repository.community;

import com.petmilyday.entity.community.MeetupPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupPostRepository extends JpaRepository<MeetupPost, Long> {

}