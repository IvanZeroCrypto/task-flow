package com.example.task_flow.memberships;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipsRepository extends JpaRepository<Memberships,Long>{
	boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);
	Optional<Memberships> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

}

