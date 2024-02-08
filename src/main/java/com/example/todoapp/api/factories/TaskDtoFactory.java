package com.example.todoapp.api.factories;

import com.example.todoapp.api.dto.TaskDto;
import com.example.todoapp.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {

    public TaskDto makeTaskDto(TaskEntity entity) {
        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
