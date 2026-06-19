package com.petmilyday.entity.hospital;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHospitalReview is a Querydsl query type for HospitalReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHospitalReview extends EntityPathBase<HospitalReview> {

    private static final long serialVersionUID = 1061252008L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHospitalReview hospitalReview = new QHospitalReview("hospitalReview");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QHospital hospital;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isReported = createBoolean("isReported");

    public final com.petmilyday.entity.member.QMember member;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final com.petmilyday.entity.reservation.QReservation reservation;

    public QHospitalReview(String variable) {
        this(HospitalReview.class, forVariable(variable), INITS);
    }

    public QHospitalReview(Path<? extends HospitalReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHospitalReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHospitalReview(PathMetadata metadata, PathInits inits) {
        this(HospitalReview.class, metadata, inits);
    }

    public QHospitalReview(Class<? extends HospitalReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hospital = inits.isInitialized("hospital") ? new QHospital(forProperty("hospital")) : null;
        this.member = inits.isInitialized("member") ? new com.petmilyday.entity.member.QMember(forProperty("member")) : null;
        this.reservation = inits.isInitialized("reservation") ? new com.petmilyday.entity.reservation.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
    }

}

