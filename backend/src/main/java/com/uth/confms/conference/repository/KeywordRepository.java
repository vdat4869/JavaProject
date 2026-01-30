package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.Keyword;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
  Optional<Keyword> findByName(String name); // Tìm từ khóa theo tên
}
