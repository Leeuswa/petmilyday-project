package com.petmilyday.domain.member;

import com.petmilyday.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}