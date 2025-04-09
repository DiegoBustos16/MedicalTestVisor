package com.visor.test_microservice.dto;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.entity.ImageStack;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
public class UpdateTestDTO {
    private Set<FileAttachment> attachments;
    private Set<ImageStack> imageStacks;
}
