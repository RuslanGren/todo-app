package com.example.todoapp.store.services;

import com.example.todoapp.api.dto.AckDto;
import com.example.todoapp.api.dto.ProjectDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectService {
    List<ProjectDto> fetchProjects(Optional<String> optionalPrefixName);

    AckDto deleteProject(Long projectId);

    ProjectDto createOrUpdateProject(Optional<Long> optionalProjectId, Optional<String> optionalProjectName);
}
