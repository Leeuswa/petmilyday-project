package com.petmilyday.repository.community;

import com.petmilyday.entity.community.MeetupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MeetupPostRepository extends JpaRepository<MeetupPost, Long> {

    @Query(value = "SELECT m FROM MeetupPost m JOIN FETCH m.host",
            countQuery = "SELECT count(m) FROM MeetupPost m")
    Page<MeetupPost> findAllWithHost(Pageable pageable);
}