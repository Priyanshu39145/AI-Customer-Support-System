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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class CompanyPolicyService {

    private final DataLoader dataLoader;
    private final DataTransformer dataTransformer;
    private final VectorStore vectorStore;
    private final CompanyPolicyRepository companyPolicyRepository;


    //We execute addCompanyPolicy in an Async way using CompletableFuture ----
    @Async
    public CompletableFuture<Void> processPolicy(Resource pdf){
        addCompanyPolicy(pdf);
        return CompletableFuture.completedFuture(null);
    }


    @Transactional
    @CacheEvict(value = "policySearch", allEntries = true)  // ADD THIS
    public String addCompanyPolicy(Resource pdf) {
        //If the resource is null or corrupted --- we say that we cant add pdf
        if (pdf == null) {
            return "Couldn't add pdf";
        }



        //We name the file unknown-policy.pdf --- if the pdf name is corrupted ---
        String fileName = pdf.getFilename() != null ? pdf.getFilename() : "unknown-policy.pdf";
        //We generate a Hash so that we can uniquely identify duplicate uploads ----
        String fileHash = generateHash(pdf);
        //Before going further go see the CompanyPolicy Entity ---

        // Duplicate check: skip ingestion when this company already uploaded the same file content.
        if (companyPolicyRepository.existsByFileHash(fileHash)) {
            return "Policy already exists. Skipping ingestion.";
        }

        // Versioning: same file name with changed content becomes the next policy version.
        //We find the file with the latest version of the same name with the current file ---
        //So if we have a new file --- and there exists an old file with the same name --- then we add version + 1 to the old version
        int version = companyPolicyRepository
                .findTopByFileNameOrderByVersionDesc(fileName)
                .map(policy -> policy.getVersion() + 1)
                .orElse(1);


        //We first convert the pdf text into verious documents using dataLoader ---
        // and then split those documents into smaller chunks of documents using dataTransformer
        List<Document> documents =
                dataTransformer.transform(dataLoader.loadDocumentsFromPDF(pdf));

        LocalDateTime uploadTime = LocalDateTime.now();
        //AtomicInteger is an Integer that is thread safe --- and prevents race condition ---
        AtomicInteger chunkCounter = new AtomicInteger(1);

        // Metadata: copy each chunk's metadata safely and add fields used for better RAG retrieval.
        //We add metadata with each document so that we can do RAG retrieval efficiently -----
        //We add fileName, fileHash, uploadTime, chunkCount and version as the metadata ----
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
        // Hashing: SHA-256 gives a stable fingerprint for duplicate PDF detection.
        //Using try with resource --- so that the resource InputStream gets auto closed ---
        //Imagine pdf as an entire book --- pdf.getInputStream allows us to read the pdf part by part ---
        try (InputStream inputStream = pdf.getInputStream()) {
            //Each stream of the pdf goes through a SHA-256 Machine which calculates the MD5 fingerprint ---
            //This statement creates an SHA-256 calculator instances ----
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Intead of reading the whole pdf at once --- we read the pdf part by part ----
            //We read the pdf 8192 bytes once
            byte[] buffer = new byte[8192];
            int bytesRead; //Stores the number of bytesRead ----
            //bytesRead = inputStream.read(buffer) ---- it reads 8192 bytes from the pdf each time
            //If we reach the end of file --- we get -1
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead); //We feed data inside the MD5 calculator ---
            }
            //We get the raw binary hashed data from the SHA-256 calculator ---
            byte[] hashBytes = digest.digest();
            //Now we create the final hash string ---- in StringBuilder
            StringBuilder hash = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hash.append(String.format("%02x", hashByte)); //We append each hashed binary data in hex format to the String
            }
            return hash.toString(); //We return the string ----
        }
        catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not generate policy file hash", e);
        }
    }

}
