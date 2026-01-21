package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(RoleName name);
}
