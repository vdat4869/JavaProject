package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import com.uth.confms.auth.enums.RoleName;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  @Enumerated(EnumType.STRING)
  private RoleName name;

  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @Builder.Default
  private Set<Permission> permissions = new HashSet<>();

}
