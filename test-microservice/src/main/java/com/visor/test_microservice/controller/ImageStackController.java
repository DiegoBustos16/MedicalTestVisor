package com.visor.test_microservice.controller;

import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.service.ImageStackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tests/image-stacks")
public class ImageStackController {

    private final ImageStackService imageStackService;

    @Operation(
            summary = "Create Image Stack",
            description = "Creates a new image stack associated with an existing test. The test ID must be valid.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image Stack created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Invalid Test ID",
                                            summary = "Test not found",
                                            value = "{\"error\": \"Invalid test ID format\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Test does not exist or is deleted\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Internal Server Error\"}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ImageStack> createImageStack(@RequestBody @Valid ImageStack imageStack) {
        return ResponseEntity.ok(imageStackService.createImageStack(imageStack));
    }

    @Operation(
            summary = "Get Image Stacks by Test ID",
            description = "Retrieves all image stacks associated with a specific test, identified by its ID.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image stacks retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            value = "{\"error\": \"Test does not exist or is deleted\"}")
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/test/{testEntityId}")
    public ResponseEntity<List<ImageStack>> getImageStacksByTestEntityId(@PathVariable String testEntityId) {
        return ResponseEntity.ok(imageStackService.getImageStacksByTestEntityId(testEntityId));
    }

    @Operation(
            summary = "Delete Image Stack",
            description = "Deletes an image stack using its unique ID. If the stack doesn't exist, a 404 is returned.",
            security = @SecurityRequirement(name = "security_auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image stack deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image stack not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Image stack not found with given ID\"}"
                            )
                    )),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageStack(@PathVariable String id) {
        imageStackService.deleteImageStack(id);
        return ResponseEntity.ok().build();
    }
}
