package com.example.task_flow.tasks;



import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Setter 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name = "tasks")
@Entity

public class Tasks{
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title" , nullable = false)
	private String title;

	@Column(name = "done" , nullable = false )
	private Boolean done = false;

	@Column(name = "workspace_id" , nullable = false)
	private Long workspaceId;

	@Column(name = "author_id" , nullable = false)
	private Long authorId;

	@CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}