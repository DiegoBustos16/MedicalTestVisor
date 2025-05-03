package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.service.FileAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tests/file-attachments")
public class FileAttachmentController {

    private final FileAttachmentService fileAttachmentService;

    @Operation(
            summary = "Upload File Attachment",
            description = "Uploads a generic file attachment (e.g. PDF, ZIP, DOCX) to S3 and associates it with a specific Test entity. Requires multipart/form-data.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded and saved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Missing file or testId\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Test with given Id does not exist or is deleted\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error during upload",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Error uploading file to S3\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{error: Internal Server Error}"
                            )
                    )
            )
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> saveFileAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("testId") String testId) {
        return ResponseEntity.ok(fileAttachmentService.saveFileAttachment(file, testId));
    }

    @Operation(
            summary = "Get Attachments by Test ID",
            description = "Returns all file attachments associated with the given Test entity ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No attachments found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"No file attachments found for test ID 'abc123'\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    )
            )
    })
    @GetMapping("/test/{testEntityId}")
    public ResponseEntity<List<FileAttachment>> getAttachmentsByTestEntityId(@PathVariable String testEntityId) {
        return ResponseEntity.ok(fileAttachmentService.getAttachmentsByTestEntityId(testEntityId));
    }

    @Operation(
            summary = "Delete File Attachment",
            description = "Deletes a specific file attachment by ID. The file is also removed from S3.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"File attachment with ID 'xyz789' not found\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFileAttachment(@PathVariable String id) {
        fileAttachmentService.deleteFileAttachment(id);
        return ResponseEntity.ok().build();
    }
}