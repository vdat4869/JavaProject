package com.uth.confms.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho các tổ chức (trường đại học, viện nghiên cứu)
 */
@Entity
@Table(name = "organizations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String code; // Ví dụ: UTH, HUST, VNU

    private String city;

    @Builder.Default
    private boolean active = true;
}
