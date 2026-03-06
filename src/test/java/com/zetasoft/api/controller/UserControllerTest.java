package com.zetasoft.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zetasoft.api.model.dto.UserRequest;
import com.zetasoft.api.model.entity.User;
import com.zetasoft.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should return all users")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Should return user by id")
    void getUserById_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void getUserById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create new user")
    void createUser_ShouldCreateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .name("New User")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    @DisplayName("Should return 400 for invalid input")
    void createUser_ShouldReturn400_WhenInvalidInput() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("")  // Invalid: blank
                .email("invalid-email")  // Invalid: not an email
                .password("123")  // Invalid: too short
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Should return 409 for duplicate username")
    void createUser_ShouldReturn409_WhenUsernameExists() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("testuser")  // Already exists
                .email("another@example.com")
                .password("password123")
                .name("Another User")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should update existing user")
    void updateUser_ShouldUpdateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword")
                .name("Updated Name")
                .build();

        mockMvc.perform(put("/api/v1/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("Should delete user")
    void deleteUser_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + testUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void deleteUser_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }
}
