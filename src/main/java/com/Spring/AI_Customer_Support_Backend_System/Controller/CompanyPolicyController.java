package com.Spring.AI_Customer_Support_Backend_System.Controller;

import com.Spring.AI_Customer_Support_Backend_System.Services.CompanyPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/company-policy")
@Slf4j
public class CompanyPolicyController {

    private final CompanyPolicyService companyPolicyService;
    private static final long MAX_SIZE = 20 * 1024 * 1024;


    //This controller is used by the admin to upload pdf of company policies so that the AI can read it and respond to the user inside the AI Chat
    //Can be uploaded only by admin ----
    //We accept MultipartFile from the user ----
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {

        log.info("Company policy upload request received");
        log.info("Uploaded filename: {}", file.getOriginalFilename());
        log.info("Uploaded content type: {}", file.getContentType());
        log.info("Uploaded file size: {} bytes", file.getSize());

        // Check if file is empty
        if (file.isEmpty()) {
            log.warn("Upload failed: File is empty");
            return ResponseEntity.badRequest().body("File is empty");
        }

        // Check if it's a PDF
        if(!isPdf(file)){
            log.warn("Upload failed: Invalid PDF file - {}", file.getOriginalFilename());
            return ResponseEntity
                    .badRequest()
                    .body("Invalid PDF file");
        }

        if(file.getSize() > MAX_SIZE){
            log.warn("Upload failed: File exceeds 20MB limit. Size={} bytes", file.getSize());
            return ResponseEntity
                    .badRequest()
                    .body("File exceeds 20MB limit");
        }

        try {
            // Convert MultipartFile → Resource
            byte[] bytes = file.getBytes();

            Resource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            log.info("Successfully converted MultipartFile to Resource");

            // You can now use this resource (for RAG / parsing / storing)
            System.out.println("PDF received: " + file.getOriginalFilename());

            log.info("Starting asynchronous policy processing for {}", file.getOriginalFilename());

            //Start Async process and say that the uploading has started ----
            companyPolicyService.processPolicy(resource);

            log.info("Policy processing task submitted successfully");

            return ResponseEntity.accepted()
                    .body("Policy upload started");

        } catch (Exception e) {
            log.error("Error uploading company policy PDF: {}", e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading PDF");
        }
    }

    private boolean isPdf(MultipartFile file) {
        try {
            byte[] header = file.getInputStream().readNBytes(5);

            String magic = new String(header);

            boolean isPdf = magic.equals("%PDF-");

            log.info("PDF validation for {}: {}", file.getOriginalFilename(), isPdf);

            return isPdf;
        }
        catch(IOException e){
            log.error("Failed to validate PDF: {}", e.getMessage(), e);
            return false;
        }
    }
}