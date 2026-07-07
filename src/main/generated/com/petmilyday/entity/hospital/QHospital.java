package com.petmilyday.entity.hospital;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHospital is a Querydsl query type for Hospital
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHospital extends EntityPathBase<Hospital> {

    private static final long serialVersionUID = 665021488L;

    public static final QHospital hospital = new QHospital("hospital");

    public final StringPath address = createString("address");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath department = createString("department");

    public final ListPath<HospitalHours, QHospitalHours> hours = this.<HospitalHours, QHospitalHours>createList("hours", HospitalHours.class, QHospitalHours.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<HospitalImg, QHospitalImg> images = this.<HospitalImg, QHospitalImg>createList("images", HospitalImg.class, QHospitalImg.class, PathInits.DIRECT2);

    public final BooleanPath isEmergency = createBoolean("isEmergency");

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final NumberPath<Integer> maxPerSlot = createNumber("maxPerSlot", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final NumberPath<Double> rating = createNumber("rating", Double.class);

    public final NumberPath<Integer> slotIntervalMin = createNumber("slotIntervalMin", Integer.class);

    public QHospital(String variable) {
        super(Hospital.class, forVariable(variable));
    }

    public QHospital(Path<? extends Hospital> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHospital(PathMetadata metadata) {
        super(Hospital.class, metadata);
    }

}

