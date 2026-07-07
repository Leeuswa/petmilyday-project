package com.petmilyday.repository.community;

import com.petmilyday.entity.community.MeetupPost;
import com.petmilyday.entity.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MeetupPostRepository extends JpaRepository<MeetupPost, Long> {

    @Query(value = "SELECT m FROM MeetupPost m JOIN FETCH m.host",
            countQuery = "SELECT count(m) FROM MeetupPost m")
    Page<MeetupPost> findAllWithHost(Pageable pageable);

    List<MeetupPost> findByHostOrderByIdDesc(Member host);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE MeetupPost m SET m.currentParticipants = m.currentParticipants + 1 WHERE m.id = :id")
    void forceAddParticipant(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE MeetupPost m SET m.status = :status WHERE m.id = :id")
    void forceUpdateStatus(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("status") com.petmilyday.entity.community.MeetupStatus status);
}