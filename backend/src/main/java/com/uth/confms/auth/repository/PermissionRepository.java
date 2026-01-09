package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByName(String name);
}
