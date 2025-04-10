package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.service.ImageFileService;
import com.visor.test_microservice.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tests/image-files")
public class ImageFileController {

    @Autowired
    private ImageFileService imageFileService;

    @Autowired
    private S3Service s3Service;

    @Operation(
            summary = "Upload Image File",
            description = "Uploads an image file to S3 and associates it with an existing Image Stack. Requires multipart/form-data.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Image file uploaded and saved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Missing file or imageStackId\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Error during file upload or processing",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Error uploading file to S3\"}"
                            )
                    ))
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> saveImageFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageStackId") String imageStackId) {

        ImageFile imageFile = new ImageFile();
        try {
            String url = s3Service.uploadFile(file);
            imageFile.setFileUrl(url);
            imageFile.setImageStackId(imageStackId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error uploading file to S3\"}");
        }

        ImageFile savedImage = imageFileService.saveImageFile(imageFile);
        return new ResponseEntity<>(savedImage, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get Image Files by Image Stack ID",
            description = "Retrieves all image files associated with a specific image stack ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No image files found for the given stack ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"No image files found for stack ID 'abc123'\"}"
                            )
                    ))
    })
    @GetMapping("/stack/{imageStackId}")
    public ResponseEntity<List<ImageFile>> getImageFilesByImageStackId(@PathVariable String imageStackId) {
        List<ImageFile> files = imageFileService.getImageFilesByImageStackId(imageStackId);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete Image File",
            description = "Deletes a specific image file by its ID. The image will also be removed from S3.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image file deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image file not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Image file with ID 'xyz789' not found\"}"
                            )
                    ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageFile(@PathVariable String id) {
        imageFileService.deleteImageFile(id);
        return ResponseEntity.noContent().build();
    }
}