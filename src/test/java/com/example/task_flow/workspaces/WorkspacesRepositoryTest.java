package com.example.task_flow.workspaces;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.task_flow.common.enums.Role;
import com.example.task_flow.memberships.Memberships;
import com.example.task_flow.user.User;

@DataJpaTest 
public class WorkspacesRepositoryTest{

	@Autowired TestEntityManager em;
	@Autowired WorkspacesRepository repo;

	private User persistUser(String nickname) {
        User u = new User();
        u.setNickname(nickname);
        u.setEmail(nickname + "@mail.com");     
        u.setPassword("password-hash");          
        return em.persist(u);
    }

    private Workspaces persistWorkspace(String name, Long ownerId) {
        Workspaces w = new Workspaces();
        w.setName(name);
        w.setOwnerId(ownerId);                   
        return em.persist(w);
    }

    private Memberships persistMembership(Long userId, Long workspaceId, Role role) {
        Memberships m = new Memberships();
        m.setUserId(userId);
        m.setWorkspaceId(workspaceId);
        m.setRole(role);
        return em.persist(m);
    }

    @Test 
    @DisplayName("успешное получение всех workspace где находится пользователь")
    void getMyWorkspace(){
    	User alice  = persistUser("alice");

    	Workspaces w1 = persistWorkspace("Team 1", alice.getId());
    	Workspaces w2 = persistWorkspace("Team 2", alice.getId());

    	persistMembership(alice.getId(), w1.getId(), Role.OWNER);
		persistMembership(alice.getId(), w2.getId(), Role.MEMBER);

		em.flush();

		List<Workspaces> results = repo.findWorkspaces(alice.getId());

		assertThat(results).hasSize(2)
            .extracting(dto -> dto.getId())
            .containsExactlyInAnyOrder(w1.getId(),w2.getId());
    }

    @Test 
    @DisplayName("пустой список групп пользователя")
    void getMyWorkspaceEmpty(){
    	User alice  = persistUser("alice");
		em.flush();

		List<Workspaces> results = repo.findWorkspaces(alice.getId());

		assertThat(results).isEmpty();
	}
}