package com.example.dao;

import com.example.entity.User;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserDao userDao;

    @BeforeAll
    void setUpAll() {
        // Настраиваем Hibernate для тестовой БД
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
        clearDatabase();
    }

    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @AfterAll
    static void tearDownAll() {
        HibernateUtil.shutdown();
    }

    private void clearDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        }
    }

    @Test
    void testSaveUser() {
        // Given
        User user = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        User savedUser = userDao.save(user);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals(30, savedUser.getAge());
    }

    @Test
    void testFindById() {
        // Given
        User user = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .age(25)
                .build();
        User savedUser = userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("Jane Doe", foundUser.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<User> foundUser = userDao.findById(999L);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindAll() {
        // Given
        userDao.save(User.builder().name("User1").email("user1@test.com").age(20).build());
        userDao.save(User.builder().name("User2").email("user2@test.com").age(25).build());

        // When
        List<User> users = userDao.findAll();

        // Then
        assertEquals(2, users.size());
    }

    @Test
    void testFindAll_Empty() {
        // When
        List<User> users = userDao.findAll();

        // Then
        assertTrue(users.isEmpty());
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = User.builder()
                .name("Original Name")
                .email("original@example.com")
                .age(30)
                .build();
        User savedUser = userDao.save(user);

        // When
        savedUser.setName("Updated Name");
        savedUser.
                setEmail("updated@example.com");
        savedUser.setAge(35);
        User updatedUser = userDao.update(savedUser);

        // Then
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(35, updatedUser.getAge());
    }

    @Test
    void testDeleteUser() {
        // Given
        User user = User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .age(40)
                .build();
        User savedUser = userDao.save(user);

        // When
        userDao.delete(savedUser.getId());

        // Then
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByEmail() {
        // Given
        User user = User.builder()
                .name("Email Test")
                .email("test@example.com")
                .age(28)
                .build();
        userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        Optional<User> foundUser = userDao.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }
}