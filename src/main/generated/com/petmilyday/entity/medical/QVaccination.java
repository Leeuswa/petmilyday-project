package com.petmilyday.entity.medical;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVaccination is a Querydsl query type for Vaccination
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVaccination extends EntityPathBase<Vaccination> {

    private static final long serialVersionUID = 84107882L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVaccination vaccination = new QVaccination("vaccination");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> nextDate = createDate("nextDate", java.time.LocalDate.class);

    public final com.petmilyday.entity.member.QPetProfile pet;

    public final DatePath<java.time.LocalDate> vaccinatedDate = createDate("vaccinatedDate", java.time.LocalDate.class);

    public final StringPath vaccineName = createString("vaccineName");

    public QVaccination(String variable) {
        this(Vaccination.class, forVariable(variable), INITS);
    }

    public QVaccination(Path<? extends Vaccination> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVaccination(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVaccination(PathMetadata metadata, PathInits inits) {
        this(Vaccination.class, metadata, inits);
    }

    public QVaccination(Class<? extends Vaccination> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.pet = inits.isInitialized("pet") ? new com.petmilyday.entity.member.QPetProfile(forProperty("pet"), inits.get("pet")) : null;
    }

}

