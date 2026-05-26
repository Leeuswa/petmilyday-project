package com.petmilyday.repository.member;

import com.petmilyday.entity.member.PetProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetProfileRepository extends JpaRepository<PetProfile,Long> {
}
