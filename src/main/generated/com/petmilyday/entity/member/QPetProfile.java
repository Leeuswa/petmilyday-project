package com.petmilyday.entity.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPetProfile is a Querydsl query type for PetProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPetProfile extends EntityPathBase<PetProfile> {

    private static final long serialVersionUID = -1766383584L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPetProfile petProfile = new QPetProfile("petProfile");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final StringPath breed = createString("breed");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMember member;

    public final StringPath name = createString("name");

    public final StringPath species = createString("species");

    public QPetProfile(String variable) {
        this(PetProfile.class, forVariable(variable), INITS);
    }

    public QPetProfile(Path<? extends PetProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPetProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPetProfile(PathMetadata metadata, PathInits inits) {
        this(PetProfile.class, metadata, inits);
    }

    public QPetProfile(Class<? extends PetProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

