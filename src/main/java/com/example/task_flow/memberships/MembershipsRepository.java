package com.example.task_flow.memberships;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.task_flow.common.dto.MemberDto;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface MembershipsRepository extends JpaRepository<Memberships,Long>{
	boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);
	void deleteByUserId(Long userId);
	Optional<Memberships> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

	@Query(
	"""
	SELECT new com.example.task_flow.common.dto.MemberDto(u.id,u.nickname,m.role)
	FROM Memberships m JOIN User u 
	ON u.id = m.userId WHERE m.workspaceId = :workspaceId
	"""
	)
    List<MemberDto> getAllWorkspaceUsers(@Param("workspaceId") Long workspaceId);

    @Query(
	"""
	SELECT new com.example.task_flow.common.dto.MemberDto(u.id,u.nickname,m.role)
	FROM Memberships m JOIN User u 
	ON u.id = m.userId WHERE m.workspaceId = :workspaceId AND u.id = :userId
	"""
	)
    Optional<MemberDto> getWorkspaceUser(@Param("workspaceId") Long userId,Long workspaceId);
}

