package com.example.task_flow.tasks;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_flow.tasks.dto.TaskDto;
import com.example.task_flow.tasks.dto.TaskRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController 
@RequestMapping("/api/workspaces")
@Validated 
public class TasksController{

	private final TasksService tasksService;

	public TasksController(TasksService tasksService){
		this.tasksService = tasksService;
	}

	@PostMapping("/{workspaceId}/tasks")
	public ResponseEntity<TaskDto> createTask(
		 @PathVariable @Positive(message = "workspaceId должен быть больше 0") 
         @NotNull(message = "workspaceId не должен быть пустым") Long workspaceId,
         @RequestBody @Valid TaskRequestDto request
	){
		TaskDto task = tasksService.createTask(workspaceId, request.title());
		return ResponseEntity.status(HttpStatus.CREATED).body(task);
	}

	// @GetMapping("/{workspaceId}/tasks")
	// public ResponseEntity<List<TaskDto>> getAllWorkspaceTasks(
	// 	 @PathVariable @Positive(message = "workspaceId должен быть больше 0") 
    //      @NotNull(message = "workspaceId не должен быть пустым") Long workspaceId
	// ){
	// 	List<TaskDto> tasks = tasksService.getAllWorkspaceTasks(workspaceId);
	// 	return ResponseEntity.status(HttpStatus.CREATED).body(tasks);
	// }
}