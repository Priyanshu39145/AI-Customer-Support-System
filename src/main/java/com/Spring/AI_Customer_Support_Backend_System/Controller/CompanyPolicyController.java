package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.Services.CompanyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CompanyPolicyController {

    private final CompanyPolicyService companyPolicyService;


    @PostMapping("/upload")

    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {

        // Check if file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // Check if it's a PDF
        if (file.getContentType() == null || !file.getContentType().contains("pdf")) {
            return ResponseEntity.badRequest().body("Only PDF files are allowed");
        }

        try {
            // Convert MultipartFile → Resource
            Resource resource = file.getResource();

            // You can now use this resource (for RAG / parsing / storing)
            System.out.println("PDF received: " + file.getOriginalFilename());

            return ResponseEntity.ok(companyPolicyService.addCompanyPolicy(resource));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading PDF");
        }
    }
}
