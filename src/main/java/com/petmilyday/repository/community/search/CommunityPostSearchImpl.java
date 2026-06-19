package com.petmilyday.repository.community.search;

import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.community.QCommunityPost;
import com.petmilyday.entity.member.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CommunityPostSearchImpl extends QuerydslRepositorySupport implements CommunityPostSearch {

    public CommunityPostSearchImpl() {
        super(CommunityPost.class);
    }

    @Override
    public Page<CommunityPost> searchAll(String[] types, String keyword, boolean anonymousSearch, Pageable pageable) {

        QCommunityPost post = QCommunityPost.communityPost;
        QMember member = QMember.member;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (types != null && types.length > 0) {
            for (String type : types) {
                switch (type) {
                    case "t":
                        if (keyword != null && !keyword.isEmpty()) booleanBuilder.or(post.title.contains(keyword));
                        break;
                    case "c":
                        if (keyword != null && !keyword.isEmpty()) booleanBuilder.or(post.content.contains(keyword));
                        break;
                    case "w":
                        if (anonymousSearch) {
                            booleanBuilder.or(post.anonymous.eq(true));
                        } else if (keyword != null && !keyword.isEmpty()) {
                            booleanBuilder.or(
                                    post.anonymous.eq(false).and(
                                            member.nickname.contains(keyword).or(member.username.contains(keyword))
                                    )
                            );
                        }
                        break;
                }
            }
        }

        booleanBuilder.and(post.id.gt(0L));

        // 데이터 조회
        JPQLQuery<CommunityPost> query = from(post);
        query.leftJoin(post.member, member).fetchJoin();
        query.where(booleanBuilder);

        this.getQuerydsl().applyPagination(pageable, query);
        List<CommunityPost> list = query.fetch();

        // 전체 개수 조회
        JPQLQuery<CommunityPost> countQuery = from(post);
        countQuery.leftJoin(post.member, member); // 카운트에는 일반 조인만 사용
        countQuery.where(booleanBuilder);
        long count = countQuery.fetchCount();

        return new PageImpl<>(list, pageable, count);
    }
}