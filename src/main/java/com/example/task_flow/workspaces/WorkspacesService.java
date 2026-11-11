package com.example.task_flow.workspaces;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.task_flow.common.dto.MemberDto;
import com.example.task_flow.common.enums.Role;
import com.example.task_flow.common.exception.AccessDeniedException;
import com.example.task_flow.common.exception.ConflictException;
import com.example.task_flow.memberships.Memberships;
import com.example.task_flow.memberships.MembershipsRepository;
import com.example.task_flow.memberships.MembershipsService;
import com.example.task_flow.security.SecurityUtils;
import com.example.task_flow.user.UserDomain;
import com.example.task_flow.user.UserService;
import com.example.task_flow.workspaces.dto.WorkSpaceRequestDto;
import com.example.task_flow.workspaces.dto.WorkspaceResponseDto;

import jakarta.persistence.EntityNotFoundException;

@Service 
public class WorkspacesService{
	private static final Logger log = LoggerFactory.getLogger(WorkspacesService.class);
	private final WorkspacesRepository workspacesRepository;
	private final MembershipsService membershipsService;
	private final UserService userService;


	public WorkspacesService(
		WorkspacesRepository workspacesRepository,
		MembershipsRepository membershipsRepository,
		MembershipsService membershipsService,
		UserService userService
	){
		this.workspacesRepository = workspacesRepository;
		this.membershipsService = membershipsService;
	    this.userService = userService;
	}

	@Transactional
	public WorkspaceResponseDto createWorkspace(WorkSpaceRequestDto request){
		Long userId = SecurityUtils.getRequiredUserId();

		Workspaces workspaces = new Workspaces();
		workspaces.setName(request.name());
		workspaces.setOwnerId(userId);
		Workspaces savedWorkspaces = workspacesRepository.save(workspaces);

		membershipsService.addMember(userId, savedWorkspaces.getId(),Role.OWNER);

		return new WorkspaceResponseDto(workspaces.getId(),workspaces.getName(),workspaces.getOwnerId(),workspaces.getCreatedAt());
	}

    public List<WorkspaceResponseDto> getAllMyWorkspaces(){
		Long userId = SecurityUtils.getRequiredUserId();

    	List<Workspaces> workspaces = workspacesRepository.findMyWorkspaces(userId);
    	return workspaces.stream().map(w -> new WorkspaceResponseDto(w.getId(),w.getName(),w.getOwnerId(),w.getCreatedAt())).toList();
    }

    public WorkspaceResponseDto getByIdWorkspace(Long workspaceId){
    	Long userId = SecurityUtils.getRequiredUserId();
    	
    	boolean isMember = membershipsService.checkMember(userId, workspaceId);
    	if(!isMember){
    		throw new AccessDeniedException("Нет доступа  не участник");
    	}

    	Workspaces workspace = workspacesRepository.findById(workspaceId)
    	.orElseThrow(() -> new EntityNotFoundException("workspace не существует по id =" + workspaceId));
    	return new WorkspaceResponseDto(workspace.getId(),workspace.getName(),workspace.getOwnerId(),workspace.getCreatedAt());
    }

    public void deleteByIdWorkspace(Long workspaceId){
    	Long userId = SecurityUtils.getRequiredUserId();
    	membershipsService.checkOwner(userId, workspaceId);
    	workspacesRepository.deleteById(workspaceId);
    }

    public MemberDto addUserWorkspace(Long workspaceId,String email){
    	Long userId = SecurityUtils.getRequiredUserId();
    	membershipsService.checkOwner(userId, workspaceId);

    	UserDomain user = userService.findUserEmail(email);

    	boolean isMember = membershipsService.checkMember(user.id(), workspaceId);

    	if(isMember){
    		throw new ConflictException("Пользователь " + user.nickname() + " уже участник:");
    	}

    	Memberships result = membershipsService.addMember(user.id(), workspaceId, Role.MEMBER);

    	return new MemberDto(user.id(),user.nickname(),result.getRole());
    }
	
}