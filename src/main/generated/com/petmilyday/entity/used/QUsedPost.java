package com.petmilyday.entity.used;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUsedPost is a Querydsl query type for UsedPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsedPost extends EntityPathBase<UsedPost> {

    private static final long serialVersionUID = -1850050864L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUsedPost usedPost = new QUsedPost("usedPost");

    public final StringPath category = createString("category");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<UsedPostImg, QUsedPostImg> images = this.<UsedPostImg, QUsedPostImg>createList("images", UsedPostImg.class, QUsedPostImg.class, PathInits.DIRECT2);

    public final BooleanPath isHidden = createBoolean("isHidden");

    public final EnumPath<ItemCondition> itemCondition = createEnum("itemCondition", ItemCondition.class);

    public final com.petmilyday.entity.member.QMember member;

    public final BooleanPath offerAccepted = createBoolean("offerAccepted");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath region = createString("region");

    public final NumberPath<Integer> reportCount = createNumber("reportCount", Integer.class);

    public final EnumPath<UsedPostStatus> status = createEnum("status", UsedPostStatus.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QUsedPost(String variable) {
        this(UsedPost.class, forVariable(variable), INITS);
    }

    public QUsedPost(Path<? extends UsedPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUsedPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUsedPost(PathMetadata metadata, PathInits inits) {
        this(UsedPost.class, metadata, inits);
    }

    public QUsedPost(Class<? extends UsedPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.petmilyday.entity.member.QMember(forProperty("member")) : null;
    }

}

