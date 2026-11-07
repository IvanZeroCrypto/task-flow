package com.example.task_flow.workspaces;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository 
public interface WorkspacesRepository extends  JpaRepository<Workspaces,Long>{

	@Query("SELECT w FROM Workspaces w JOIN Memberships m ON  m.workspace_id  = w.id  WHERE m.userId = :userId")
	List<Workspaces> findMyWorkspaces(@Param("userId") Long userId);

}

