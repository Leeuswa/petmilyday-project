package com.petmilyday.repository.member;

import com.petmilyday.dto.admin.MemberSearchDTO;
import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.QMember;
import com.petmilyday.entity.member.Role;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> searchMembersPage(MemberSearchDTO searchDTO, Pageable pageable) {
        QMember member = QMember.member;

        List<Member> content = queryFactory
                .selectFrom(member)
                .where(
                        keywordContains(searchDTO.getKeyword()),
                        roleEq(searchDTO.getRole()),
                        statusEq(searchDTO.getStatus())
                )
                .orderBy(member.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        keywordContains(searchDTO.getKeyword()),
                        roleEq(searchDTO.getRole()),
                        statusEq(searchDTO.getStatus())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        QMember member = QMember.member;

        return member.username.containsIgnoreCase(keyword)
                .or(member.nickname.containsIgnoreCase(keyword))
                .or(member.email.containsIgnoreCase(keyword));
    }

    private BooleanExpression roleEq(Role role) {
        if (role == null) {
            return null;
        }

        return QMember.member.role.eq(role);
    }

    private BooleanExpression statusEq(AccountStatus status) {
        if (status == null) {
            return null;
        }

        return QMember.member.status.eq(status);
    }
}
