package com.example.task_flow.workspaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface WorkspacesRepository extends  JpaRepository<Workspaces,Long>{}

