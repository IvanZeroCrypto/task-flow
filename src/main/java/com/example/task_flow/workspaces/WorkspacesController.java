package com.example.task_flow.workspaces;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_flow.common.dto.MemberDto;
import com.example.task_flow.workspaces.dto.EmailRequestDto;
import com.example.task_flow.workspaces.dto.WorkSpaceRequestDto;
import com.example.task_flow.workspaces.dto.WorkspaceResponseDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

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

	@GetMapping
	public ResponseEntity<List<WorkspaceResponseDto>> getAllMyWorkspaces(){
		List<WorkspaceResponseDto> workspaces = workspacesService.getAllMyWorkspaces();
		return ResponseEntity.ok().body(workspaces);
	}

	@GetMapping("/{id}")
	public ResponseEntity<WorkspaceResponseDto> getByIdWorspace(@PathVariable @Positive(message="Id должен быть больше 0") Long id){
		WorkspaceResponseDto workspace = workspacesService.getByIdWorkspace(id);
		return ResponseEntity.ok().body(workspace);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteByIdWorspace(@PathVariable Long id){
		workspacesService.deleteByIdWorkspace(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/{id}/members")
	public ResponseEntity<MemberDto> addUserWorkspace(@PathVariable Long id,@RequestBody EmailRequestDto request){
		MemberDto member = workspacesService.addUserWorkspace(id,request.email());
		return ResponseEntity.ok().body(member);
	}

}