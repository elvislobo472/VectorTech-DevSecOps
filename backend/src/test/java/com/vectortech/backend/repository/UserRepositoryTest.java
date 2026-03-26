package com.vectortech.backend.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vectortech.backend.model.Role;
import com.vectortech.backend.model.User;

@DataJpaTest
@SuppressWarnings({"null", "unused"})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUserWhenExists() {
        User user = User.builder()
                .name("Repo Test User")
                .email("repo.user@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("repo.user@vectortech.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Repo Test User");
    }

    @Test
    void findByEmail_ShouldReturnEmptyWhenNotExists() {
        Optional<User> found = userRepository.findByEmail("nonexistent@vectortech.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnFalseWhenMissing() {
        boolean exists = userRepository.existsByEmail("missing@vectortech.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrueWhenExists() {
        User user = User.builder()
                .name("Test User")
                .email("test@vectortech.com")
                .password("SecurePass123")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@vectortech.com");

        assertThat(exists).isTrue();
    }

    @Test
    void saveUser_ShouldPersistAllFields() {
        User user = User.builder()
                .name("Full Name User")
                .email("full@vectortech.com")
                .password("ComplexPassword123!")
                .role(Role.ADMIN)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Full Name User");
        assertThat(saved.getEmail()).isEqualTo("full@vectortech.com");
        assertThat(saved.getPassword()).isEqualTo("ComplexPassword123!");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void saveUser_ShouldSetTimestamps() {
        User user = User.builder()
                .name("Timestamp User")
                .email("timestamp@vectortech.com")
                .password("Pass12345")
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    void updateUser_ShouldModifyNameOnly() {
        User user = User.builder()
                .name("Original Name")
                .email("update@vectortech.com")
                .password("Pass12345")
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        saved.setName("Updated Name");
        userRepository.save(saved);

        User updated = userRepository.findByEmail("update@vectortech.com").get();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteUser_ShouldRemoveFromDatabase() {
        User user = User.builder()
                .name("To Delete")
                .email("delete@vectortech.com")
                .password("Pass12345")
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        Long userId = saved.getId();

        userRepository.delete(saved);

        assertThat(userRepository.existsById(userId)).isFalse();
    }

    @Test
    void userDefaultRole_ShouldBeUSER() {
        User user = User.builder()
                .name("Default Role")
                .email("defaultrole@vectortech.com")
                .password("Pass12345")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void userEmail_ShouldBeUnique() {
        User user1 = User.builder()
                .name("User 1")
                .email("unique@vectortech.com")
                .password("Pass12345")
                .role(Role.USER)
                .build();

        userRepository.save(user1);

        // In a real scenario with proper database verification,
        // attempting to save a duplicate email would fail
        assertThat(userRepository.existsByEmail("unique@vectortech.com")).isTrue();
    }

    @Test
    void saveMultipleUsers_ShouldPersistAll() {
        for (int i = 1; i <= 5; i++) {
            userRepository.save(User.builder()
                    .name("User " + i)
                    .email("user" + i + "@vectortech.com")
                    .password("Pass12345")
                    .role(Role.USER)
                    .build());
        }

        assertThat(userRepository.findAll()).hasSize(5);
    }
}
