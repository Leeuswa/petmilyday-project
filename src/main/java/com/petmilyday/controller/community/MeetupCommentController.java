package com.petmilyday.controller.community;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.dto.community.MeetupCommentDTO;
import com.petmilyday.service.community.MeetupCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/meetup-comments")
@Log4j2
@RequiredArgsConstructor
public class MeetupCommentController {

    private final MeetupCommentService meetupCommentService; // 🌟 모임 전용 서비스 연결!

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> register(@Valid @RequestBody MeetupCommentDTO meetupCommentDTO, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymousUser";
        Long id = meetupCommentService.register(username, meetupCommentDTO);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("id", id);
        return resultMap;
    }

    @GetMapping("/list/{meetupPostId}")
    public PageResponseDTO<MeetupCommentDTO> getList(@PathVariable("meetupPostId") Long meetupPostId,
                                                     PageRequestDTO pageRequestDTO) {
        return meetupCommentService.getListOfMeetupPost(meetupPostId, pageRequestDTO);
    }

    @GetMapping("/{id}")
    public MeetupCommentDTO getCommentDTO(@PathVariable("id") Long id) {
        return meetupCommentService.read(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> modify(@PathVariable("id") Long id, @RequestBody MeetupCommentDTO meetupCommentDTO) {
        meetupCommentDTO.setId(id);
        meetupCommentService.modify(meetupCommentDTO);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("id", id);
        return resultMap;
    }

    @DeleteMapping("/{id}")
    public Map<String, Long> remove(@PathVariable("id") Long id) {
        meetupCommentService.remove(id);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("id", id);
        return resultMap;
    }

    @PostMapping("/{id}/like")
    public Map<String, Object> toggleLike(@PathVariable("id") Long id, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymousUser";
        int latestLikeCount = meetupCommentService.toggleLike(username, id);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("likeCount", latestLikeCount);
        return resultMap;
    }
}