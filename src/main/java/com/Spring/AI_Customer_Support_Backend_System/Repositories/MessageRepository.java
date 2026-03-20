package com.Spring.AI_Customer_Support_Backend_System.Repositories;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {
}