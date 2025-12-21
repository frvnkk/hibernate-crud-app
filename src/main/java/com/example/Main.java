package com.example;

import com.example.entity.User;
import com.example.service.UserService;
import com.example.util.HibernateUtil;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Scanner;

@Log4j2
public class Main {

    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        log.info("Starting Hibernate CRUD application");

        try {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        getUser();
                        break;
                    case "3":
                        getAllUsers();
                        break;
                    case "4":
                        updateUser();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (Exception e) {
            log.error("Application error: {}", e.getMessage(), e);
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
            log.info("Application finished");
        }
    }

    private static void printMenu() {
        System.out.println("\n=== CRUD User Management Application ===");
        System.out.println("1. Create user");
        System.out.println("2. Find user by ID");
        System.out.println("3. Show all users");
        System.out.println("4. Update user");
        System.out.println("5. Delete user");
        System.out.println("6. Exit");
        System.out.print("Choose action: ");
    }

    private static void createUser() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine());

            User user = userService.createUser(name, email, age);
            System.out.println("User created: ID=" + user.getId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getUser() {
        try {
            System.out.print("Enter user ID: ");
            long id = Long.parseLong(scanner.nextLine());

            userService.getUserById(id).ifPresentOrElse(
                    user -> System.out.printf("User found: ID=%d, Name=%s, Email=%s, Age=%d%n",
                            user.getId(), user.getName(), user.getEmail(), user.getAge()),
                    () -> System.out.println("User not found")
            );
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found");
            } else {
                System.out.println("User list:");
                for (User user : users) {
                    System.out.printf("- ID=%d, Name=%s, Email=%s, Age=%d, Created=%s%n",
                            user.getId(), user.getName(), user.getEmail(), user.getAge(),
                            user.getCreatedAt());
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.
                    getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Enter user ID to update: ");
            long id = Long.parseLong(scanner.nextLine());

            System.out.print("Enter new name: ");
            String name = scanner.nextLine();

            System.out.print("Enter new email: ");
            String email = scanner.nextLine();

            System.out.print("Enter new age: ");
            int age = Integer.parseInt(scanner.nextLine());

            User user = userService.updateUser(id, name, email, age);
            System.out.println("User updated: ID=" + user.getId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Enter user ID to delete: ");
            long id = Long.parseLong(scanner.nextLine());

            userService.deleteUser(id);
            System.out.println("User deleted");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}