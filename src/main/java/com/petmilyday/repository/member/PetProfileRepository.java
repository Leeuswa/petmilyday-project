package com.petmilyday.repository.member;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetProfileRepository extends JpaRepository<PetProfile,Long> {

    List<PetProfile> findByMember(Member member);

    Optional<PetProfile> findByIdAndMember(Long id, Member member);
}
