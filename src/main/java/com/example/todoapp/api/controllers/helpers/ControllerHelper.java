package com.example.todoapp.api.controllers.helpers;

import com.example.todoapp.api.exceptions.NotFoundException;
import com.example.todoapp.store.entities.ProjectEntity;
import com.example.todoapp.store.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ControllerHelper {
    private final ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with %d doesn't exists.", projectId))
                );
    }
}
