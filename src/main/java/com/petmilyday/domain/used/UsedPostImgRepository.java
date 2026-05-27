package com.petmilyday.domain.used;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsedPostImgRepository extends JpaRepository<UsedPostImg, Long> {
}