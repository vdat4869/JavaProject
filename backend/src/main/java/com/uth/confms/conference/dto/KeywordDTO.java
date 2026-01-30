package com.uth.confms.conference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordDTO {
  private Long id;
  private String name; // Tên từ khóa
  private String description; // Mô tả
}
