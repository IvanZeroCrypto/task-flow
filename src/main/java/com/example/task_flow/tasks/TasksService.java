package com.example.task_flow.tasks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.task_flow.common.exception.AccessDeniedException;
import com.example.task_flow.memberships.MembershipsService;
import com.example.task_flow.security.SecurityUtils;
import com.example.task_flow.tasks.dto.TaskDto;

@Service 
public class TasksService{

	private final TasksRepository tasksRepository;
	private final MembershipsService membershipsService;


	public TasksService(TasksRepository tasksRepository,MembershipsService membershipsService){
		this.tasksRepository = tasksRepository;
		this.membershipsService = membershipsService;
	}

	public TaskDto createTask(Long workspaceId,String title){
		Long userId = SecurityUtils.getRequiredUserId();

		boolean isMember =  membershipsService.checkMember(userId, workspaceId);
		if(!isMember){
			throw new AccessDeniedException("Нет доступа вы не участник:");
		}

		Tasks task = new Tasks();
		task.setTitle(title);
		task.setWorkspaceId(workspaceId);
		task.setAuthorId(userId);

		Tasks savedTask =  tasksRepository.save(task);

		return new TaskDto(
			savedTask.getId(),
			savedTask.getTitle(),
			savedTask.getDone(),
			savedTask.getWorkspaceId(),
			savedTask.getAuthorId(),
			savedTask.getCreatedAt(),
			savedTask.getUpdatedAt()
			);
	}
	// public List<TaskDto> getAllWorkspaceTasks(Long workspaceId){
		
	// }
}