package com.petmilyday.repository.member;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 아이디 중복 확인에 사용
    boolean existsByUsername(String username);

    // 이메일 중복 확인에 사용
    boolean existsByEmail(String email);

    // 로그인할 때 회원 정보를 찾아오는 기능
    Optional<Member> findByUsername(String username);

    //이메일 찾기
    Optional<Member> findByEmail(String email);

    //권한 찾ㅣ
    List<Member> findByRole(Role role);
}