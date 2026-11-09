package com.example.task_flow.memberships;

import com.example.task_flow.common.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@Entity 
@Table(name = "memberships")
@NoArgsConstructor 
@AllArgsConstructor 
public class Memberships {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id" , nullable = false)
	private Long userId;

	@Column(name = "workspace_id" , nullable = false )
	private Long workspaceId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false,length = 10)
	private Role role;

}