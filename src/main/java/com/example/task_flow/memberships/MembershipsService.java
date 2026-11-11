package com.example.task_flow.memberships;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.task_flow.common.dto.MemberDto;
import com.example.task_flow.common.enums.Role;
import com.example.task_flow.common.exception.AccessDeniedException;
import com.example.task_flow.security.SecurityUtils;
import com.example.task_flow.user.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service 
public class MembershipsService{

	private final MembershipsRepository membershipsRepository;
	// private final UserService userService;


	public MembershipsService(MembershipsRepository membershipsRepository,UserService userService){
		this.membershipsRepository = membershipsRepository;
		// this.userService = userService;
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
	
	// @Transactional 
	// public MemberDto updateWorkspaceMemberRole(Long workspaceId,Long targetUserId,Role role){
	// 	Long userId = SecurityUtils.getRequiredUserId();

	//      checkOwner(userId,workspaceId);
   //      boolean isMember = checkMember(targetUserId, workspaceId);
   //      if(!isMember){
   //      	throw new EntityNotFoundException("Not found member");
   //      }

   //      Memberships m = membershipsRepository.findByUserIdAndWorkspaceId(targetUserId,workspaceId)
   //      .orElseThrow(() -> new EntityNotFoundException("not found entity workspaces"));

   //      if(role != Role.OWNER){
   //      	m.setRole(role);
   //       Memberships savedMember = 	membershipsRepository.save(m);
   //      	UserDomain user = userService.findUserById(userId);
   //      	return new MemberDto(user.id(),user.nickname(),savedMember.getRole());

   //      }else{
   //      	m.setRole(role);
   //      	membershipsRepository.save(m);

   //      	Memberships oldwnerUser = membershipsRepository.findByUserIdAndWorkspaceId(userId,workspaceId)
   //         .orElseThrow(() -> new EntityNotFoundException("not found entity workspaces"));

   //          oldwnerUser.setRole(Role.ADMIN);
   //       Memberships savedMember = 	membershipsRepository.save(oldwnerUser);
   //      	UserDomain user = userService.findUserById(targetUserId);
   //      	return new MemberDto(user.id(),user.nickname(),savedMember.getRole());
   //      }
       
	// }
}