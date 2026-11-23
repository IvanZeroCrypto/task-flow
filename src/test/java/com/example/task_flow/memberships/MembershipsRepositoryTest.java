package com.example.task_flow.memberships;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.task_flow.common.dto.MemberDto;
import com.example.task_flow.common.enums.Role;
import com.example.task_flow.user.User;
import com.example.task_flow.workspaces.Workspaces;

@DataJpaTest 
public class MembershipsRepositoryTest{

	@Autowired TestEntityManager em;
	@Autowired MembershipsRepository repo;

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
	@DisplayName("MembershipsRepository успешное получение всех пользователей  в группе")
	void getAllWorkspaceUsers_returnsOnlyMembersOfWorkspace(){
		User alice  = persistUser("alice");
		User tomas = persistUser("tomas");
		User bob  = persistUser("bob");

		Workspaces w1 = persistWorkspace("Team 1", alice.getId());
		Workspaces w2 = persistWorkspace("Team 2", tomas.getId());

		persistMembership(alice.getId(), w1.getId(), Role.OWNER);
		persistMembership(tomas.getId(), w2.getId(), Role.OWNER);
		persistMembership(bob.getId(), w1.getId(), Role.MEMBER);

		em.flush();

		List<MemberDto> results = repo.getAllWorkspaceUsers(w1.getId());

		assertThat(results).hasSize(2)
            .extracting(dto -> dto.userId())
            .containsExactlyInAnyOrder(alice.getId(), bob.getId());
	}

	@Test
	@DisplayName("MembershipsRepository получаем пустой список так как никого нет в  группе")
    void getAllWorkspaceUsers_returnsEmpty_whenWorkspaceHasNoMembers() {
        User owner = persistUser("owner");
        Workspaces empty = persistWorkspace("Empty", owner.getId());

        em.flush();

        List<MemberDto> result = repo.getAllWorkspaceUsers(empty.getId());

        assertThat(result).isEmpty();
    }

    @Test
	@DisplayName("MembershipsRepository успешное получение одного пользователя из групы по id ")
    void getWorkspaceUser() {
        User alice  = persistUser("alice");
        Workspaces w1 = persistWorkspace("Team 1", alice.getId());
        persistMembership(alice.getId(), w1.getId(), Role.OWNER);

        em.flush();

        Optional<MemberDto> result = repo.getWorkspaceUser(w1.getId(),alice.getId());

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(alice.getId());
        assertThat(result.get().role()).isEqualTo(Role.OWNER);
    }

    @Test
	@DisplayName("MembershipsRepository пользователя нет в группе ответ пустой ")
    void getWorkspaceUserErrorResponseEmpty() {
        User alice  = persistUser("alice");
		User tomas = persistUser("tomas");

        Workspaces w1 = persistWorkspace("Team 1", alice.getId());
        persistMembership(alice.getId(), w1.getId(), Role.OWNER);

        em.flush();

        Optional<MemberDto> result = repo.getWorkspaceUser(w1.getId(),tomas.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByUserId_removesAllMembershipsOfUser_andKeepsOthers() {
        User alice = persistUser("alice");
        User bob   = persistUser("bob");

        Workspaces ws1 = persistWorkspace("Team 1", alice.getId());
        Workspaces ws2 = persistWorkspace("Team 2", bob.getId());

        persistMembership(alice.getId(), ws1.getId(), Role.ADMIN);
        persistMembership(alice.getId(), ws2.getId(), Role.MEMBER); 
        persistMembership(bob.getId(),   ws1.getId(), Role.MEMBER);
        em.flush();

        repo.deleteByUserId(alice.getId());
        em.flush();

        assertThat(repo.findByUserIdAndWorkspaceId(alice.getId(), ws1.getId())).isEmpty();
        assertThat(repo.findByUserIdAndWorkspaceId(alice.getId(), ws2.getId())).isEmpty();
        assertThat(repo.findByUserIdAndWorkspaceId(bob.getId(), ws1.getId())).isPresent();
    }


}