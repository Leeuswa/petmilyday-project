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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class PetProfileServiceImpl implements PetProfileService {

    private final PetProfileRepository petProfileRepository;
    private final MemberRepository memberRepository;



    //반려동물 정보 가져오기
    @Override
    public List<PetProFileDTO> petList(String loginId) {

        Member member = memberRepository.findByUsername(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 ID가 올바르지 않습니다."));

        List<PetProfile> petProfileList = petProfileRepository.findByMember(member);

        return petProfileList.stream()
                .map(pet -> PetProFileDTO.builder()
                        .id(pet.getId())
                        .name(pet.getName())
                        .build()
        ).collect(Collectors.toList());
    }
}
