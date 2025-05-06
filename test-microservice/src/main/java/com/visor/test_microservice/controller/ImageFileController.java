package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.service.ImageFileService;
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
@RequestMapping("/api/tests/image-files")
public class ImageFileController {

    private final ImageFileService imageFileService;

    @Operation(
            summary = "Upload Image File",
            description = "Uploads an image file to S3 and associates it with an existing Image Stack. Requires multipart/form-data.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image file uploaded and saved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Missing file or imageStackId\"}"
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Image Stack does not exist or is deleted\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Error during file upload or processing",
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
    public ResponseEntity<ImageFile> saveImageFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageStackId") String imageStackId) {
        return ResponseEntity.ok(imageFileService.saveImageFile(file, imageStackId));
    }

    @Operation(
            summary = "Get Image Files by Image Stack ID",
            description = "Retrieves all image files associated with a specific image stack ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"ImageStack does not exist or is deleted'\"}"
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
    @GetMapping("/stack/{imageStackId}")
    public ResponseEntity<List<ImageFile>> getImageFilesByImageStackId(@PathVariable String imageStackId) {
        return ResponseEntity.ok(imageFileService.getImageFilesByImageStackId(imageStackId));
    }

    @Operation(
            summary = "Delete Image File",
            description = "Deletes a specific image file by its ID. The image will also be removed from S3.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image file deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image file not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Image file with ID 'xyz789' not found\"}"
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
    public ResponseEntity<Void> deleteImageFile(@PathVariable String id) {
        imageFileService.deleteImageFile(id);
        return ResponseEntity.ok().build();
    }
}