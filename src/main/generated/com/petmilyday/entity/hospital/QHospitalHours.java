package com.petmilyday.entity.hospital;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHospitalHours is a Querydsl query type for HospitalHours
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHospitalHours extends EntityPathBase<HospitalHours> {

    private static final long serialVersionUID = -944535361L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHospitalHours hospitalHours = new QHospitalHours("hospitalHours");

    public final TimePath<java.time.LocalTime> closeTime = createTime("closeTime", java.time.LocalTime.class);

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final QHospital hospital;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isClosed = createBoolean("isClosed");

    public final TimePath<java.time.LocalTime> openTime = createTime("openTime", java.time.LocalTime.class);

    public QHospitalHours(String variable) {
        this(HospitalHours.class, forVariable(variable), INITS);
    }

    public QHospitalHours(Path<? extends HospitalHours> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHospitalHours(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHospitalHours(PathMetadata metadata, PathInits inits) {
        this(HospitalHours.class, metadata, inits);
    }

    public QHospitalHours(Class<? extends HospitalHours> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hospital = inits.isInitialized("hospital") ? new QHospital(forProperty("hospital")) : null;
    }

}

