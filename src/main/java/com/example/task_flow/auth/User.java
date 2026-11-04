package com.example.task_flow.auth;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Setter 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name = "users")
@Entity

public class User{
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nickname" , nullable = false , length = 20,
		columnDefinition = "VARCHAR(20) CHECK(length(nickname) >= 3)"
	 )
	private String nickname;

	@Column(name = "email" , nullable = false , unique = true , length = 100)
	private String email;

	@Column(name = "password" , nullable = false , length = 60)
	private String password;

	@Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}