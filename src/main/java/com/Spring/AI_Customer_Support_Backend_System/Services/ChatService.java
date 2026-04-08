package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.DTO.AIResponse;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Conversation;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Message;
import com.Spring.AI_Customer_Support_Backend_System.Entities.Type.SenderType;
import com.Spring.AI_Customer_Support_Backend_System.Entities.User;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.ConversationRepository;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final AIService aiService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;

    public AIResponse chat(String message, User user, String conversationId)  {

        Conversation conversation = conversationService.getConversationById(conversationId);
        if(conversation==null)
            throw new IllegalArgumentException("Converstion doesnt exist");




        Message usermessage = Message.builder()
                .content(message)
                .conversation(conversation)
                .sender(SenderType.USER)
                .senderUser(user)
                .build();

        messageRepository.save(usermessage);

        if (conversation.getTitle() == null) {
            String title = aiService.generateTitle(message);

            conversation.setTitle(title);
            conversationRepository.save(conversation);
        }

        List<Message> messages = messageRepository
                .findByConversationOrderByCreatedAtAsc(conversation);
        StringBuilder history = new StringBuilder();

        for (Message msg : messages) {
            if (msg.getSender() == SenderType.USER) {
                history.append("User: ").append(msg.getContent()).append("\n");
            } else {
                history.append("AI: ").append(msg.getContent()).append("\n");
            }
        }

        AIResponse response = aiService.askAIWithHistory(message,history);

        Message aimessage = Message.builder()
                .content(response.getAiResponse())
                .sender(SenderType.AI)
                .conversation(conversation)
                .build();

        messageRepository.save(aimessage);

        return response;

    }
}
