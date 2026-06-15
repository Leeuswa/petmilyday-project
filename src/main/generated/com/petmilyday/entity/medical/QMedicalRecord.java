package com.petmilyday.entity.medical;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMedicalRecord is a Querydsl query type for MedicalRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMedicalRecord extends EntityPathBase<MedicalRecord> {

    private static final long serialVersionUID = 672483623L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMedicalRecord medicalRecord = new QMedicalRecord("medicalRecord");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath diagnosis = createString("diagnosis");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath memo = createString("memo");

    public final StringPath pdfUrl = createString("pdfUrl");

    public final com.petmilyday.entity.member.QPetProfile pet;

    public final StringPath prescription = createString("prescription");

    public final com.petmilyday.entity.reservation.QReservation reservation;

    public final StringPath treatment = createString("treatment");

    public final BooleanPath vaccinated = createBoolean("vaccinated");

    public final StringPath vaccineName = createString("vaccineName");

    public final DatePath<java.time.LocalDate> visitDate = createDate("visitDate", java.time.LocalDate.class);

    public QMedicalRecord(String variable) {
        this(MedicalRecord.class, forVariable(variable), INITS);
    }

    public QMedicalRecord(Path<? extends MedicalRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMedicalRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMedicalRecord(PathMetadata metadata, PathInits inits) {
        this(MedicalRecord.class, metadata, inits);
    }

    public QMedicalRecord(Class<? extends MedicalRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.pet = inits.isInitialized("pet") ? new com.petmilyday.entity.member.QPetProfile(forProperty("pet"), inits.get("pet")) : null;
        this.reservation = inits.isInitialized("reservation") ? new com.petmilyday.entity.reservation.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
    }

}

