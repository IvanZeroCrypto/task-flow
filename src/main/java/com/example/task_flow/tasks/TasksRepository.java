package com.example.task_flow.tasks;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface TasksRepository  extends JpaRepository<Tasks,Long>{
	List<Tasks> findAllByWorkspaceId(Long workspaceId);
	List<Tasks> findAllByWorkspaceIdAndDone(Long workspaceId,Boolean done);

	
}
