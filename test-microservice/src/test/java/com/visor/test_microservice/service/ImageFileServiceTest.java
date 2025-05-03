package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.ImageFile;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.ImageFileRepository;
import com.visor.test_microservice.repository.ImageStackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ImageFileServiceTest {

    @Mock
    private ImageFileRepository imageFileRepository;

    @Mock
    private ImageStackRepository imageStackRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ImageFileService imageFileService;

    @Nested
    @DisplayName("Create ImageFile")
    class CreateImageFileTests {

        @Test
        @DisplayName("should save ImageFile when imageStackId valid")
        void createImageFile_shouldSave_whenValid() throws IOException {
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1]);
            given(imageStackRepository.existsById("valid-stack-id")).willReturn(true);
            given(imageFileRepository.save(any(ImageFile.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(s3Service.uploadFile(file)).willReturn("http://example.com/image.jpg");

            ImageFile result = imageFileService.saveImageFile(file, "valid-stack-id");

            assertThat(result).isNotNull();
            assertThat(result.getFileUrl()).isEqualTo("http://example.com/image.jpg");
            assertThat(result.getImageStackId()).isEqualTo("valid-stack-id");
            verify(imageFileRepository).save(any(ImageFile.class));
        }

        @Test
        @DisplayName("should throw when imageStack not found")
        void createImageFile_shouldThrow_whenImageStackIdNotFound() {
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[0]);
            given(imageStackRepository.existsById("missing-stack-id")).willReturn(false);

            assertThatThrownBy(() -> imageFileService.saveImageFile(file, "missing-stack-id"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active Image Stack found with ID: missing-stack-id");
        }

        @Test
        @DisplayName("should throw when file is empty")
        void createImageFile_shouldThrow_whenFileIsEmpty() {
            MultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);
            given(imageStackRepository.existsById("valid-stack-id")).willReturn(true);

            assertThatThrownBy(() -> imageFileService.saveImageFile(file, "valid-stack-id"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("The file is empty or null");
        }

        @Test
        @DisplayName("should throw when file upload fails")
        void createImageFile_shouldThrow_whenFileUploadFails() throws IOException {
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[0]);
            given(imageStackRepository.existsById("valid-stack-id")).willReturn(true);
            given(s3Service.uploadFile(file)).willThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> imageFileService.saveImageFile(file, "valid-stack-id"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error uploading file to S3: Upload failed");
        }
    }

    @Nested
    @DisplayName("Get ImageFile by ImageStack ID")
    class GetImageFileByImageStackIdTests {

        @Test
        @DisplayName("should return ImageFiles when imageStackId valid")
        void getImageFilesByImageStackId_shouldReturn_whenValid() {
            String imageStackId = "valid-stack-id";
            ImageFile imageFile = new ImageFile();
            imageFile.setImageStackId(imageStackId);
            given(imageStackRepository.existsById(imageStackId)).willReturn(true);
            given(imageFileRepository.findByImageStackIdAndDeletedAtIsNull(imageStackId))
                    .willReturn(List.of(imageFile));

            List<ImageFile> result = imageFileService.getImageFilesByImageStackId(imageStackId);

            assertThat(result).isNotEmpty().hasSize(1).extracting(ImageFile::getImageStackId).containsExactly(imageStackId);
            verify(imageFileRepository).findByImageStackIdAndDeletedAtIsNull(imageStackId);
        }

        @Test
        @DisplayName("should throw when imageStackId not found")
        void getImageFilesByImageStackId_shouldThrow_whenNotFound() {
            String imageStackId = "missing-stack-id";
            given(imageStackRepository.existsById(imageStackId)).willReturn(false);

            assertThatThrownBy(() -> imageFileService.getImageFilesByImageStackId(imageStackId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active Image Stack found with ID: " + imageStackId);
        }
    }

    @Nested
    @DisplayName("Delete ImageFile")
    class DeleteImageFileTests {

        @Test
        @DisplayName("should set deletedAt when exists")
        void deleteImageFile_shouldSetDeletedAt_whenExists() {
            String id = "valid-file-id";
            ImageFile existing = new ImageFile();
            existing.setId(id);

            given(imageFileRepository.findByIdAndDeletedAtIsNull(id)).willReturn(java.util.Optional.of(existing));
            given(imageFileRepository.save(any(ImageFile.class))).willAnswer(invocation -> invocation.getArgument(0));

            imageFileService.deleteImageFile(id);

            assertThat(existing.getDeletedAt()).isNotNull();
            verify(imageFileRepository).save(existing);
        }

        @Test
        @DisplayName("should throw when not found")
        void deleteImageFile_shouldThrow_whenNotFound() {
            String id = "missing-file-id";
            given(imageFileRepository.findByIdAndDeletedAtIsNull(id)).willReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> imageFileService.deleteImageFile(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active image file found with ID: " + id);
        }
    }
}
