package com.petmilyday.repository;

import com.petmilyday.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 아이디 중복 확인에 사용 [cite: 1]
    boolean existsByUsername(String username);

    // 이메일 중복 확인에 사용
    boolean existsByEmail(String email);

    // 로그인할 때 회원 정보를 찾아오는 기능
    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);
}