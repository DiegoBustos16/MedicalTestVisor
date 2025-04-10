package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.service.ImageStackService;
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

import java.util.List;

@RestController
@RequestMapping("/api/tests/image-stacks")
public class ImageStackController {

    @Autowired
    private ImageStackService imageStackService;

    @Operation(
            summary = "Create Image Stack",
            description = "Creates a new image stack associated with an existing test. The test ID must be valid.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Image Stack created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "InvalidTest",
                                            summary = "Test not found",
                                            value = "{\"error\": \"Test does not exist\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Unexpected server error\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ImageStack> createImageStack(@RequestBody ImageStack imageStack) {
        ImageStack createdStack = imageStackService.createImageStack(imageStack);
        return new ResponseEntity<>(createdStack, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get Image Stacks by Test ID",
            description = "Retrieves all image stacks associated with a specific test, identified by its ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image stacks retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No image stacks found for the given test ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"No image stacks found for test ID\"}"
                            )
                    ))
    })
    @GetMapping("/test/{testEntityId}")
    public ResponseEntity<List<ImageStack>> getImageStacksByTestEntityId(@PathVariable String testEntityId) {
        List<ImageStack> stacks = imageStackService.getImageStacksByTestEntityId(testEntityId);
        return new ResponseEntity<>(stacks, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete Image Stack",
            description = "Deletes an image stack using its unique ID. If the stack doesn't exist, a 404 is returned.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image stack deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image stack not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Image stack not found\"}"
                            )
                    ))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageStack(@PathVariable String id) {
        imageStackService.deleteImageStack(id);
        return ResponseEntity.noContent().build();
    }
}
