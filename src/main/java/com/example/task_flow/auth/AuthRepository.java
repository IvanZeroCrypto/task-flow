package com.example.task_flow.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface AuthRepository extends JpaRepository<User,Long>{
	boolean existsByEmail(String email);
}