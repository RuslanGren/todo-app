package com.example.todoapp.api.factories;

import com.example.todoapp.api.dto.TaskStateDto;
import com.example.todoapp.store.entities.TaskStateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskStateDtoFactory {
    private final TaskDtoFactory taskDtoFactory;

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {
        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .leftTaskStateId(entity.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .tasks(
                        entity
                                .getTasks()
                                .stream()
                                .map(taskDtoFactory::makeTaskDto)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
