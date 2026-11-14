package com.example.task_flow.tasks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.task_flow.common.exception.AccessDeniedException;
import com.example.task_flow.memberships.MembershipsService;
import com.example.task_flow.security.SecurityUtils;
import com.example.task_flow.tasks.dto.TaskDto;
import com.example.task_flow.tasks.dto.TaskPatchDto;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service 
public class TasksService{

	private final TasksRepository tasksRepository;
	private final MembershipsService membershipsService;


	public TasksService(TasksRepository tasksRepository,MembershipsService membershipsService){
		this.tasksRepository = tasksRepository;
		this.membershipsService = membershipsService;
	}

	public boolean isTaskFromWorkspace(Long workspaceId,Long taskId){
		Tasks task = tasksRepository.findById(taskId)
		.orElseThrow(() -> new EntityNotFoundException("task not found workspaceId " + workspaceId + ""));
	     
	    return task.getWorkspaceId().equals(workspaceId);
	}

	public boolean isAuthorTask(Long workspaceId,Long taskId,Long authorId){
		Tasks task = tasksRepository.findById(taskId)
		.orElseThrow(() -> new EntityNotFoundException("task not found workspaceId " + workspaceId + ""));
	     
	    return task.getAuthorId().equals(authorId);
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

	public List<TaskDto> getAllWorkspaceTasks(Long workspaceId,Boolean done){
		Long userId = SecurityUtils.getRequiredUserId();

		boolean isMember =  membershipsService.checkMember(userId, workspaceId);
		if(!isMember){
			throw new AccessDeniedException("Нет доступа вы не участник:");
		}

		List<Tasks> tasks = (done == null) ? 
		tasksRepository.findAllByWorkspaceId(workspaceId) : tasksRepository.findAllByWorkspaceIdAndDone(workspaceId,done);
		
		return tasks.stream().map(t -> 
			new TaskDto(
			t.getId(),
			t.getTitle(),
			t.getDone(),
			t.getWorkspaceId(),
			t.getAuthorId(),
			t.getCreatedAt(),
			t.getUpdatedAt()
			)).toList();
	}

	public TaskDto getWorkspaceTaskById(Long workspaceId,Long taskId){
		Long userId = SecurityUtils.getRequiredUserId();

		boolean isMember =  membershipsService.checkMember(userId, workspaceId);
		if(!isMember){
			throw new AccessDeniedException("Нет доступа вы не участник:");
		}

		if(!isTaskFromWorkspace(workspaceId, taskId)){
			throw new EntityNotFoundException("not found task workspace");
		}

		Tasks task = tasksRepository.findById(taskId)
		.orElseThrow(() -> new EntityNotFoundException("task not found taskId "));

		return new TaskDto(
			task.getId(),
			task.getTitle(),
			task.getDone(),
			task.getWorkspaceId(),
			task.getAuthorId(),
			task.getCreatedAt(),
			task.getUpdatedAt()
			);
	}
	public TaskDto updateWorkspaceTaskById(TaskPatchDto request,Long workspaceId,Long taskId){
		Long userId = SecurityUtils.getRequiredUserId();

		boolean isMember =  membershipsService.checkMember(userId, workspaceId);
		if(!isMember){
			throw new AccessDeniedException("Нет доступа вы не участник:");
		}

		if(!isTaskFromWorkspace(workspaceId, taskId)){
			throw new EntityNotFoundException("not found task workspace");
		}

		Tasks task = tasksRepository.findById(taskId)
		.orElseThrow(() -> new EntityNotFoundException("task not found workspaceId " + workspaceId + ""));

		if(request.title() != null){
			task.setTitle(request.title());
		}
		if(request.done() != null){
			task.setDone(request.done());
		}

		Tasks taskUpdated = tasksRepository.save(task);

		return new TaskDto(
			taskUpdated.getId(),
			taskUpdated.getTitle(),
			taskUpdated.getDone(),
			taskUpdated.getWorkspaceId(),
			taskUpdated.getAuthorId(),
			taskUpdated.getCreatedAt(),
			taskUpdated.getUpdatedAt()
			);
	}

	@Transactional
	public void removeWorkspaceTaskById(Long workspaceId,Long taskId){
		Long userId = SecurityUtils.getRequiredUserId();

		boolean isMember =  membershipsService.checkMember(userId, workspaceId);
		if(!isMember){
			throw new AccessDeniedException("Нет доступа вы не участник:");
		}

		if(!isTaskFromWorkspace(workspaceId, taskId)){
			throw new EntityNotFoundException("not found task workspace");
		}

		if(isAuthorTask(workspaceId, taskId, userId)){
			tasksRepository.deleteById(taskId);
		}else{
			membershipsService.checkOwner(userId, workspaceId);
			tasksRepository.deleteById(taskId);
		}
	}
}

