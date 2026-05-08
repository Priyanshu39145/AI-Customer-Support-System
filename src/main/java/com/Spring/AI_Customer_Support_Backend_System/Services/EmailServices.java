package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.Entities.Ticket;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailServices {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("classpath:Email-Content.html")
    Resource emailContent;

    @Async
    public void sendEmail(Ticket ticket)   {

        if(ticket==null)
            return;


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        String resolvedTime = ticket.getUpdatedAt() != null
                ? ticket.getUpdatedAt().format(formatter)
                : "Recently";


        try {

            String content = emailContent.getContentAsString(StandardCharsets.UTF_8)
                    .replace("{{userName}}", ticket.getCreatedBy().getName())
                    .replace("{{ticketId}}", ticket.getId())
                    .replace("{{description}}", ticket.getDescription())
                    .replace("{{priority}}", ticket.getPriority().toString())
                    .replace("{{agentName}}", ticket.getAssignedTo().getName())
                    .replace("{{resolvedAt}}",resolvedTime) // or resolved time
                    .replace("{{supportLink}}", "http://your-app.com/support")
                    .replace("{{unsubscribeLink}}", "http://your-app.com/unsubscribe");
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mime,true);
            message.setFrom(fromEmail);
            message.setTo(ticket.getCreatedBy().getEmail());
            message.setSubject("Your Ticket is Closed");
            message.setText(content, true);


            mailSender.send(mime);
//            return "Message sent";
        }
        catch (Exception e) {
//            return e.getMessage();
            System.out.println(e.getMessage());
        }
//        return null;
    }
}
