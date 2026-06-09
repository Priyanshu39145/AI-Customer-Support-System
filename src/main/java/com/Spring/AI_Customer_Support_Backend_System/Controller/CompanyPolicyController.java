package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.Services.CompanyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/company-policy")
public class CompanyPolicyController {

    private final CompanyPolicyService companyPolicyService;
    private static final long MAX_SIZE = 20 * 1024 * 1024;


    //This controller is used by the admin to upload pdf of company policies so that the AI can read it and respond to the user inside the AI Chat
    //Can be uploaded only by admin ----
    //We accept MultipartFile from the user ----
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {

        // Check if file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // Check if it's a PDF
        if(!isPdf(file)){
            return ResponseEntity
                    .badRequest()
                    .body("Invalid PDF file");
        }

        if(file.getSize() > MAX_SIZE){
            return ResponseEntity
                    .badRequest()
                    .body("File exceeds 20MB limit");
        }

        try {
            // Convert MultipartFile → Resource
            Resource resource = file.getResource();

            // You can now use this resource (for RAG / parsing / storing)
            System.out.println("PDF received: " + file.getOriginalFilename());

            //Start Async process and say that the uploading has started ----
            companyPolicyService.processPolicy(resource);

            return ResponseEntity.accepted()
                    .body("Policy upload started");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading PDF");
        }
    }

    private boolean isPdf(MultipartFile file) {
        try {
            byte[] header = file.getInputStream().readNBytes(5);

            String magic = new String(header);

            return magic.equals("%PDF-");
        }
        catch(IOException e){
            return false;
        }
    }
}
