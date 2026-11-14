package com.example.task_flow.memberships;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.task_flow.common.dto.MemberDto;
import com.example.task_flow.common.enums.Role;
import com.example.task_flow.common.exception.AccessDeniedException;
import com.example.task_flow.security.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service 
public class MembershipsService{

	private final MembershipsRepository membershipsRepository;


	public MembershipsService(MembershipsRepository membershipsRepository){
		this.membershipsRepository = membershipsRepository;
	}

	public Memberships checkOwner(Long userId,Long workspaceId){
		Memberships m = membershipsRepository.findByUserIdAndWorkspaceId(userId,workspaceId)
		.orElseThrow(() -> new EntityNotFoundException("not found entity workspaces"));
		if(m.getRole() != Role.OWNER){
			throw new AccessDeniedException("Нет доступа вы не владелец");
		}
		return m;
	}

		public boolean checkAdmin(Long userId,Long workspaceId){
		Memberships m = membershipsRepository.findByUserIdAndWorkspaceId(userId,workspaceId)
		.orElseThrow(() -> new EntityNotFoundException("not found entity workspaces"));
		if(m.getRole() != Role.ADMIN){
			return false;
		}
		return true;
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

	public List<MemberDto> getAllMembers(Long workspaceId){
		Long userId = SecurityUtils.getRequiredUserId();

		boolean isMember = checkMember(userId, workspaceId);

		if(!isMember){
			throw new AccessDeniedException("Нет доступа вы не участник:");
		}

		List<MemberDto> members = membershipsRepository.getAllWorkspaceUsers(workspaceId);

		return members;
	}

	@Transactional
	public void removeUserFromWorkspace(Long workspaceId,Long targetUserId){
		Long userId = SecurityUtils.getRequiredUserId();
        if(userId.equals(targetUserId)){
        	throw new IllegalStateException("Невозможно удалить самого себя");
        }

        checkOwner(userId,workspaceId);

        boolean isMember = checkMember(targetUserId, workspaceId);
        if(!isMember){
        	throw new EntityNotFoundException("Not found member");
        }
        membershipsRepository.deleteByUserId(targetUserId);
	}
	
	@Transactional 
	public MemberDto updateWorkspaceMemberRole(Long workspaceId,Long targetUserId,Role role){
		Long userId = SecurityUtils.getRequiredUserId();

		if(targetUserId.equals(userId)){
			throw new AccessDeniedException("Нет доступа нельзя сменить роль у самого себя");
		}

		if(role == Role.OWNER){
			throw new AccessDeniedException("Нет доступа нельзя сменить роль владельца ");
		}

        boolean isMember = checkMember(targetUserId, workspaceId);
        if(!isMember){
        	throw new EntityNotFoundException("Not found member");
        }

        if(!checkAdmin(userId, workspaceId)){
        	checkOwner(userId, workspaceId);

        	Memberships m = membershipsRepository.findByUserIdAndWorkspaceId(targetUserId,workspaceId)
		    .orElseThrow(() -> new EntityNotFoundException("not found entity workspaces"));

		    m.setRole(role);
		    membershipsRepository.save(m);

		    MemberDto memberUser = membershipsRepository.getWorkspaceUser(targetUserId, workspaceId).
		    orElseThrow(() ->  new EntityNotFoundException("member user not found"));

		    return memberUser;

        }else{
        	Memberships m = membershipsRepository.findByUserIdAndWorkspaceId(targetUserId,workspaceId)
		    .orElseThrow(() -> new EntityNotFoundException("not found entity workspaces"));
		    m.setRole(role);

		    membershipsRepository.save(m);

		    MemberDto memberUser = membershipsRepository.getWorkspaceUser(targetUserId, workspaceId).
		    orElseThrow(() ->  new EntityNotFoundException("member user not found"));

		    return memberUser;
        }
   }
}

