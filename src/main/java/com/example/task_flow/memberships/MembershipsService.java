package com.example.task_flow.memberships;

import org.springframework.stereotype.Service;

import com.example.task_flow.common.enums.Role;
import com.example.task_flow.common.exception.AccessDeniedException;

@Service 
public class MembershipsService{

	private final MembershipsRepository membershipsRepository;

	public MembershipsService(MembershipsRepository membershipsRepository){
		this.membershipsRepository = membershipsRepository;
	}

	public void checkOwner(Long userId,Long workspaceId){
		Memberships m = membershipsRepository.findByUserIdAndWorkspaceId(userId,workspaceId)
		.orElseThrow(() -> new AccessDeniedException("Нет доступа вы не владелец"));
		if(m.getRole() != Role.OWNER){
			throw new AccessDeniedException("Нет доступа вы не владелец");
		}
	}

	public boolean checkMember(Long userId,Long workspaceId){
		boolean exists = membershipsRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
		return exists;
	}

	public Memberships addMember(Long userId,Long workspaceId,Role role){
		Memberships memberships = new Memberships();
		memberships.setUserId(userId);
		memberships.setWorkspaceId(workspaceId);
		memberships.setRole(role);

    	Memberships savedMember = membershipsRepository.save(memberships);
    	return savedMember;
	}
}