package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.ImageStack;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.ImageStackRepository;
import com.visor.test_microservice.repository.TestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ImageStackServiceTest {

    @Mock
    private TestRepository testRepository;
    @Mock
    private ImageStackRepository imageStackRepository;

    @InjectMocks
    private ImageStackService imageStackService;

    @Nested
    @DisplayName("Create ImageStack")
    class CreateImageStackTests {

        @Test
        @DisplayName("should save ImageStack when test and name valid")
        void createImageStack_shouldSave_whenValid() {
            ImageStack input = new ImageStack();
            input.setStackName("Valid name");
            input.setTestId("valid-test-id");

            given(testRepository.existsByIdAndDeletedAtIsNull("valid-test-id")).willReturn(true);
            given(imageStackRepository.save(any(ImageStack.class))).willReturn(input);
            ImageStack result = imageStackService.createImageStack(input);

            assertThat(result).isNotNull();
            assertThat(result.getStackName()).isEqualTo("Valid name");
            assertThat(result.getTestId()).isEqualTo("valid-test-id");
            verify(imageStackRepository).save(any(ImageStack.class));
        }

        @Test
        @DisplayName("should throw when test not found")
        void createTestEntity_shouldThrow_whenTestNotFound() {
            ImageStack input = new ImageStack();
            input.setStackName("Valid name");
            input.setTestId("missing-test-id");

            given(testRepository.existsByIdAndDeletedAtIsNull("missing-test-id")).willReturn(false);

            assertThatThrownBy(() -> imageStackService.createImageStack(input))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active test found with ID: missing-test-id");
        }
    }

    @Nested
    @DisplayName("Get ImageStack by ID")
    class GetImageStackByTestIdTests {
        @Test
        @DisplayName("should return ImageStack when Test exists")
        void getImageStackByTestId_shouldReturn_whenExists() {
            String id = "valid-test-id";
            ImageStack stack = new ImageStack();
            stack.setTestId(id);

            given(testRepository.existsByIdAndDeletedAtIsNull(id)).willReturn(true);
            given(imageStackRepository.findByTestIdAndDeletedAtIsNull(id)).willReturn(List.of(stack));

            List<ImageStack> result = imageStackService.getImageStacksByTestEntityId(id);

            assertThat(result).isNotEmpty().hasSize(1).extracting(ImageStack::getTestId).containsExactly(id);
            verify(imageStackRepository).findByTestIdAndDeletedAtIsNull(id);
        }

        @Test
        @DisplayName("should throw when not found")
        void getImageStackByTestId_shouldThrow_whenNotFound() {
            String id = "missing-test-id";
            given(testRepository.existsByIdAndDeletedAtIsNull(id)).willReturn(false);

            assertThatThrownBy(() -> imageStackService.getImageStacksByTestEntityId(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active test found with ID: " + id);
        }
    }

    @Nested
    @DisplayName("Delete ImageStack")
    class DeleteImageStackTests {
        @Test
        @DisplayName("should set deletedAt when exists")
        void deleteImageStack_shouldSetDeletedAt_whenExists() {
            String id = "valid-stack-id";
            ImageStack existing = new ImageStack();
            existing.setTestId(id);

            given(imageStackRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(existing));
            given(imageStackRepository.save(any(ImageStack.class))).willAnswer(invocation -> invocation.getArgument(0));

            imageStackService.deleteImageStack(id);

            assertThat(existing.getDeletedAt()).isNotNull();
            verify(imageStackRepository).save(existing);
        }

        @Test
        @DisplayName("should throw when not found")
        void deleteImageStack_shouldThrow_whenNotFound() {
            String id = "missing-stack-id";
            given(imageStackRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> imageStackService.deleteImageStack(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active image stack found with ID: " + id);
        }
    }
}
