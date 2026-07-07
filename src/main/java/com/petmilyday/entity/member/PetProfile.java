package com.petmilyday.entity.member;

import com.petmilyday.entity.reservation.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "pet_profile")
@Builder
@AllArgsConstructor
public class PetProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String species;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 50)
    private String breed;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(length = 500)
    private String photoUrl;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();
}