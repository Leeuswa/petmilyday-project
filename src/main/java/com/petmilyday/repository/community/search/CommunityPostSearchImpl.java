package com.petmilyday.repository.community.search;

import com.petmilyday.entity.community.CommunityPost;
import com.petmilyday.entity.community.QCommunityPost;
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
    public Page<CommunityPost> searchAll(String[] types, String keyword, Pageable pageable) {

        QCommunityPost post = QCommunityPost.communityPost;
        JPQLQuery<CommunityPost> query = from(post);

        if ((types != null && types.length > 0) && keyword != null) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            for (String type : types) {
                switch (type) {
                    case "t":
                        booleanBuilder.or(post.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(post.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(post.member.nickname.contains(keyword));
                        break;
                }
            }
            query.where(booleanBuilder);
        }

        query.where(post.id.gt(0L));

        this.getQuerydsl().applyPagination(pageable, query);

        List<CommunityPost> list = query.fetch();
        long count = query.fetchCount();

        return new PageImpl<>(list, pageable, count);
    }
}