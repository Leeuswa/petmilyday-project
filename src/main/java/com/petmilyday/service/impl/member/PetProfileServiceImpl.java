package com.petmilyday.service.impl.member;

import com.petmilyday.dto.member.PetProFileDTO;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.service.member.PetProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PetProfileServiceImpl implements PetProfileService {

    private final PetProfileRepository petProfileRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PetProFileDTO> petList(String loginId) {
        Member member = memberRepository.findByUsername(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 ID가 올바르지 않습니다."));

        List<PetProfile> petProfileList = petProfileRepository.findByMember(member);

        return petProfileList.stream()
                .map(pet -> modelMapper.map(pet, PetProFileDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registerPet(String loginId, PetProFileDTO dto) {
        Member member = memberRepository.findByUsername(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        PetProfile petProfile = PetProfile.builder()
                .member(member)
                .name(dto.getName())
                .species(dto.getSpecies())
                .breed(dto.getBreed())
                .age(dto.getAge())
                .gender(dto.getGender())
                .photoUrl(dto.getPhotoUrl())
                .build();

        petProfileRepository.save(petProfile);
    }

    @Override
    @Transactional
    public void deletePet(Long petId, String loginId) {
        PetProfile petProfile = petProfileRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("반려동물 프로필을 찾을 수 없습니다."));

        if (!petProfile.getMember().getUsername().equals(loginId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        petProfileRepository.delete(petProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public PetProFileDTO getPet(Long petId, String loginId) {
        PetProfile petProfile = petProfileRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("반려동물 프로필을 찾을 수 없습니다."));

        if (!petProfile.getMember().getUsername().equals(loginId)) {
            throw new IllegalArgumentException("해당 프로필 조회 권한이 없습니다.");
        }

        return modelMapper.map(petProfile, PetProFileDTO.class);
    }

    @Override
    @Transactional
    public void updatePet(Long petId, PetProFileDTO dto, String loginId) {
        PetProfile petProfile = petProfileRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("반려동물 프로필을 찾을 수 없습니다."));

        if (!petProfile.getMember().getUsername().equals(loginId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        PetProfile updatedPet = PetProfile.builder()
                .id(petProfile.getId())
                .member(petProfile.getMember())
                .name(dto.getName())
                .species(dto.getSpecies())
                .breed(dto.getBreed())
                .age(dto.getAge())
                .gender(dto.getGender())
                .photoUrl(dto.getPhotoUrl())
                .build();

        petProfileRepository.save(updatedPet);
    }
}