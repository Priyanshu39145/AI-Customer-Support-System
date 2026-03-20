package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}