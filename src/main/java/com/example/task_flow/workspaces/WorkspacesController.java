package com.example.task_flow.workspaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_flow.workspaces.dto.WorkSpaceRequestDto;
import com.example.task_flow.workspaces.dto.WorkspaceResponseDto;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/workspaces")
public class WorkspacesController{
	
	private static final Logger log = LoggerFactory.getLogger(WorkspacesController.class);
	private final WorkspacesService workspacesService;

	public WorkspacesController(WorkspacesService workspacesService){
		this.workspacesService = workspacesService;
	}

	@PostMapping
	public ResponseEntity<WorkspaceResponseDto> createWorkSpace(
		@RequestBody @Valid WorkSpaceRequestDto request
	){
		log.info("Пришли данные в createWorkSpace" + request.name());
		WorkspaceResponseDto result = workspacesService.createWorkspace(request);

		log.info("получили ответ от сервиса createWorkSpace" + result);

		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}


}