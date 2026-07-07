package com.petmilyday.repository.community.search;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.community.QCommunityPost;
import com.petmilyday.entity.member.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CommunityPostSearchImpl extends QuerydslRepositorySupport implements CommunityPostSearch {

    public CommunityPostSearchImpl() {
        super(CommunityPost.class);
    }

    @Override
    public Page<CommunityPost> searchAll(PageRequestDTO pageRequestDTO) {
        QCommunityPost post = QCommunityPost.communityPost;
        JPQLQuery<CommunityPost> query = from(post);

        // N+1 문제 방지
        query.leftJoin(post.member).fetchJoin();

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        String type = pageRequestDTO.getType();
        String keyword = pageRequestDTO.getKeyword();

        if (type != null && keyword != null && !keyword.trim().isEmpty()) {
            BooleanBuilder conditionBuilder = new BooleanBuilder();
            if (type.contains("t")) {
                conditionBuilder.or(post.title.contains(keyword));
            }
            if (type.contains("c")) {
                conditionBuilder.or(post.content.contains(keyword));
            }
            if (type.contains("w")) {
                conditionBuilder.or(post.member.nickname.contains(keyword));
            }
            booleanBuilder.and(conditionBuilder);
        }

        // 익명 게시글 체크 박스 필터링 (HTML의 anonymousSearch 파라미터 연동)
        // 만약 PageRequestDTO에 isAnonymousSearch() 대신 getAnonymousSearch()가 있다면 맞춰서 변경해주세요.
        if (pageRequestDTO.isAnonymousSearch()) {
            booleanBuilder.and(post.anonymous.isTrue());
        }

        query.where(booleanBuilder);

        // 정렬 및 페이징 처리
        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() <= 0 ? 0 : pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("id").descending()
        );

        this.getQuerydsl().applyPagination(pageable, query);

        List<CommunityPost> list = query.fetch();
        long count = query.fetchCount();

        return new PageImpl<>(list, pageable, count);
    }
}