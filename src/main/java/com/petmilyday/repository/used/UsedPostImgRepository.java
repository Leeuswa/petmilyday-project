package com.petmilyday.repository.used;

import com.petmilyday.entity.used.UsedPostImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsedPostImgRepository extends JpaRepository<UsedPostImg, Long> {
}