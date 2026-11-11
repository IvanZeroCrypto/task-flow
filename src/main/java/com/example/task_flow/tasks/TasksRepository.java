package com.example.task_flow.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface TasksRepository  extends JpaRepository<Tasks,Long>{

	
}
