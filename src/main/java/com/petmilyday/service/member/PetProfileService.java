package com.petmilyday.service.member;

import com.petmilyday.dto.member.PetProFileDTO;
import com.petmilyday.entity.member.PetProfile;

import java.util.List;


public interface PetProfileService {

    List<PetProFileDTO> petList(String LoginId);
}
