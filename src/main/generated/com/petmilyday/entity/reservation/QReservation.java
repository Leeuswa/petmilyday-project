package com.petmilyday.entity.reservation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = 529713068L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final StringPath cancelReason = createString("cancelReason");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath department = createString("department");

    public final com.petmilyday.entity.hospital.QHospital hospital;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.petmilyday.entity.member.QMember member;

    public final com.petmilyday.entity.member.QPetProfile pet;

    public final DatePath<java.time.LocalDate> reserveDate = createDate("reserveDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> reserveTime = createTime("reserveTime", java.time.LocalTime.class);

    public final EnumPath<ReservationStatus> status = createEnum("status", ReservationStatus.class);

    public final NumberPath<Integer> waitNumber = createNumber("waitNumber", Integer.class);

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hospital = inits.isInitialized("hospital") ? new com.petmilyday.entity.hospital.QHospital(forProperty("hospital")) : null;
        this.member = inits.isInitialized("member") ? new com.petmilyday.entity.member.QMember(forProperty("member")) : null;
        this.pet = inits.isInitialized("pet") ? new com.petmilyday.entity.member.QPetProfile(forProperty("pet"), inits.get("pet")) : null;
    }

}

