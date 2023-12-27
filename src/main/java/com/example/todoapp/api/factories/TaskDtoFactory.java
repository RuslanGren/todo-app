package com.example.todoapp.api.factories;

import com.example.todoapp.api.dto.TaskStateDto;
import com.example.todoapp.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {

    public TaskStateDto makeTaskDto(TaskEntity entity) {
        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
