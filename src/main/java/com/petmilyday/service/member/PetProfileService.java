package com.petmilyday.service.member;

import com.petmilyday.dto.member.PetProFileDTO;
import java.util.List;

public interface PetProfileService {
    List<PetProFileDTO> petList(String loginId);
    void registerPet(String loginId, PetProFileDTO dto);
    void deletePet(Long petId, String loginId);
    PetProFileDTO getPet(Long petId, String loginId);
    void updatePet(Long petId, PetProFileDTO dto, String loginId);
}