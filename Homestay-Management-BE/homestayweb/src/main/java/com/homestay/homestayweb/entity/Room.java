package com.homestay.homestayweb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

    private String roomType;

    private Double price;

    private Boolean availability;

    @Lob
    private String features;

    private String roomStatus;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;
}
