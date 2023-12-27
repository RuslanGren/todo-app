package com.example.todoapp.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;

    private String name;

    @JsonProperty("created_at")
    private Instant createdAt;
}

