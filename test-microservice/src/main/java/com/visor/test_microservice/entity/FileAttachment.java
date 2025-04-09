package com.visor.test_microservice.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "file_attachments")
@Getter
@Setter
@NoArgsConstructor
public class FileAttachment {
    @Id
    private String id;

    @CreatedDate
    private Instant createdAt;

    private Instant deletedAt;

    private String fileName;

    private String fileUrl;

    @NotNull(message = "Test must not be null")
    private String testId;
}

