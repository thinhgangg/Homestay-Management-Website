package com.homestay.homestayweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "homestay_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomestayImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imageUrl;

    private Boolean isPrimary = false;

    @ManyToOne
    @JoinColumn(name = "homestay_id")
    private Homestay homestay;
}

