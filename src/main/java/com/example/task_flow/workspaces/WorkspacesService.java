package com.example.task_flow.workspaces;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.task_flow.common.enums.Role;
import com.example.task_flow.common.exception.UnauthorizedException;
import com.example.task_flow.memberships.Memberships;
import com.example.task_flow.memberships.MembershipsRepository;
import com.example.task_flow.security.CustomUserDetails;
import com.example.task_flow.workspaces.dto.WorkSpaceRequestDto;
import com.example.task_flow.workspaces.dto.WorkspaceResponseDto;

@Service 
public class WorkspacesService{
	private static final Logger log = LoggerFactory.getLogger(WorkspacesService.class);
	private final WorkspacesRepository workspacesRepository;
	private final MembershipsRepository membershipsRepository;

	public WorkspacesService(WorkspacesRepository workspacesRepository,MembershipsRepository membershipsRepository){
		this.workspacesRepository = workspacesRepository;
		this.membershipsRepository = membershipsRepository;
	}

	@Transactional
	public WorkspaceResponseDto createWorkspace(WorkSpaceRequestDto request){
		CustomUserDetails userDetails = getRequiredUser();
		
		Long userId = userDetails.getUserId();

		Workspaces workspaces = new Workspaces();
		workspaces.setName(request.name());
		workspaces.setOwner_id(userId);
		Workspaces savedWorkspaces = workspacesRepository.save(workspaces);

		Memberships memberships = new Memberships();
		memberships.setUserId(userId);
		memberships.setWorkspace_id(savedWorkspaces.getId());
		memberships.setRole(Role.OWNER);
		membershipsRepository.save(memberships);

		return new WorkspaceResponseDto(workspaces.getId(),workspaces.getName(),workspaces.getOwner_id(),workspaces.getCreatedAt());

	}

	private CustomUserDetails getRequiredUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
        return userDetails;
    }
    throw new IllegalStateException("Текущий пользователь не найден в SecurityContext");
}

	
}