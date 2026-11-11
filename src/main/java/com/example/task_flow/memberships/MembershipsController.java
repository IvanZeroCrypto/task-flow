package com.example.task_flow.memberships;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_flow.common.dto.MemberDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/workspaces/{workspaceId}/members")
public class MembershipsController{
	public final MembershipsService membershipsService;

	public MembershipsController(MembershipsService membershipsService){
		this.membershipsService = membershipsService;
	}

	@GetMapping
	public ResponseEntity<List<MemberDto>> getAllMembers(@PathVariable @Positive(message="workspaceId должен быть больше 0") @NotNull (message = "workspaceId не должен быть пустым") Long workspaceId){
		List<MemberDto> members = membershipsService.getAllMembers(workspaceId);
		return ResponseEntity.ok().body(members);
	}

	@DeleteMapping("/{targetUserId}")
	public ResponseEntity<Void> removeUserFromWorkspace(
		 @PathVariable @Positive(message = "workspaceId должен быть больше 0") 
         @NotNull(message = "workspaceId не должен быть пустым") Long workspaceId,
            
         @PathVariable @Positive(message = "targetUserId должен быть больше 0") 
         @NotNull(message = "targetUserId не должен быть пустым") Long targetUserId
	){
	  membershipsService.removeUserFromWorkspace(workspaceId,targetUserId);
	  return ResponseEntity.status(HttpStatus.OK).build();
	}

	// @PatchMapping ("/{targetUserId}")
	// public ResponseEntity<MemberDto> updateWorkspaceMemberRole(
	// 	 @PathVariable @Positive(message = "workspaceId должен быть больше 0") 
    //      @NotNull(message = "workspaceId не должен быть пустым") Long workspaceId,
            
    //      @PathVariable @Positive(message = "targetUserId должен быть больше 0") 
    //      @NotNull(message = "targetUserId не должен быть пустым") Long targetUserId,
    //      @RequestBody @Valid RequestUpdateWorkspaceMemberRole request
	// ){
	//   MemberDto result =  membershipsService.updateWorkspaceMemberRole(workspaceId,targetUserId,request.role());
	//   return ResponseEntity.status(HttpStatus.OK).body(result);
	// }
}