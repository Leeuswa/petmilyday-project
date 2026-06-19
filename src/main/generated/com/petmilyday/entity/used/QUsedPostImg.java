package com.petmilyday.entity.used;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUsedPostImg is a Querydsl query type for UsedPostImg
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsedPostImg extends EntityPathBase<UsedPostImg> {

    private static final long serialVersionUID = -1844873517L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUsedPostImg usedPostImg = new QUsedPostImg("usedPostImg");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imgUrl = createString("imgUrl");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final QUsedPost usedPost;

    public QUsedPostImg(String variable) {
        this(UsedPostImg.class, forVariable(variable), INITS);
    }

    public QUsedPostImg(Path<? extends UsedPostImg> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUsedPostImg(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUsedPostImg(PathMetadata metadata, PathInits inits) {
        this(UsedPostImg.class, metadata, inits);
    }

    public QUsedPostImg(Class<? extends UsedPostImg> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.usedPost = inits.isInitialized("usedPost") ? new QUsedPost(forProperty("usedPost"), inits.get("usedPost")) : null;
    }

}

