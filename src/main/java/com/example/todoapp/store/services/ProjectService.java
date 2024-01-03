package com.example.todoapp.store.services;

import com.example.todoapp.api.dto.ProjectDto;

public interface ProjectService {
    ProjectDto createProject(String name);
}
