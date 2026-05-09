package com.Spring.AI_Customer_Support_Backend_System.Services;

import com.Spring.AI_Customer_Support_Backend_System.ETLPipeline.DataLoader;
import com.Spring.AI_Customer_Support_Backend_System.ETLPipeline.DataTransformer;
import com.Spring.AI_Customer_Support_Backend_System.Entities.CompanyPolicy;
import com.Spring.AI_Customer_Support_Backend_System.Repositories.CompanyPolicyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class CompanyPolicyService {

    private final DataLoader dataLoader;
    private final DataTransformer dataTransformer;
    private final VectorStore vectorStore;
    private final CompanyPolicyRepository companyPolicyRepository;

    @Transactional
    @CacheEvict(value = "policySearch", allEntries = true)  // ADD THIS
    public String addCompanyPolicy(Resource pdf) {

        if (pdf == null) {
            return "Couldn't add pdf";
        }

        String fileName = pdf.getFilename() != null ? pdf.getFilename() : "unknown-policy.pdf";
        String fileHash = generateHash(pdf);

        // Duplicate check: skip ingestion when this company already uploaded the same file content.
        if (companyPolicyRepository.existsByFileHash(fileHash)) {
            return "Policy already exists. Skipping ingestion.";
        }

        // Versioning: same file name with changed content becomes the next policy version.
        int version = companyPolicyRepository
                .findTopByFileNameOrderByVersionDesc(fileName)
                .map(policy -> policy.getVersion() + 1)
                .orElse(1);

        List<Document> documents =
                dataTransformer.transform(dataLoader.loadDocumentsFromPDF(pdf));

        LocalDateTime uploadTime = LocalDateTime.now();
        AtomicInteger chunkCounter = new AtomicInteger(1);

        // Metadata: copy each chunk's metadata safely and add fields used for better RAG retrieval.
        List<Document> enrichedDocs = documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                    metadata.put("source", fileName);
                    metadata.put("fileHash", fileHash);
                    metadata.put("uploadTime", uploadTime.toString());
                    metadata.put("chunkId", chunkCounter.getAndIncrement());
                    metadata.put("version", version);

                    return new Document(doc.getFormattedContent(), metadata);
                })
                .toList();

        vectorStore.delete(
                "source == '" + fileName + "'"
        );

        // Storage: write enriched chunks into the vector store after duplicate/version checks.
        vectorStore.add(enrichedDocs);

        // Storage: save the policy record so future uploads can detect duplicates and versions.
        CompanyPolicy policy = CompanyPolicy.builder()
                .fileName(fileName)
                .fileHash(fileHash)
                .version(version)
                .uploadedAt(uploadTime)
                .build();
        companyPolicyRepository.save(policy);

        return "Documents are added Successfully. Version: " + version;
    }

    public String generateHash(Resource pdf) {
        // Hashing: MD5 gives a stable fingerprint for duplicate PDF detection.
        try (InputStream inputStream = pdf.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hash = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hash.append(String.format("%02x", hashByte));
            }
            return hash.toString();
        }
        catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not generate policy file hash", e);
        }
    }
}
