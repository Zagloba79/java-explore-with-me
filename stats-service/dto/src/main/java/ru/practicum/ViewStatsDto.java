package ru.practicum;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {
  private String app;
  private String uri;
  private Long hits;
}