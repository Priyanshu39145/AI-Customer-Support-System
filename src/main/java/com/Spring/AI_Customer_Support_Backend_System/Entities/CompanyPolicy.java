package com.Spring.AI_Customer_Support_Backend_System.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "company_policies")
public class CompanyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fileName;


    @Column(nullable = false, unique = true)
    private String fileHash;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}

//We have a fileName
//Filehash prevents uploading of duplicate files inside the RAG DB ----
//If we upload files of similar fileName but we have different content ---
// then we increase the version of the new file by 1 --- as it is of a new version ---
//uploadedAt stores the creation timestamp ---
