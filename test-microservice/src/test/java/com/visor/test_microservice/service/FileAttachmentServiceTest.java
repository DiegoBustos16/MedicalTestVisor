package com.visor.test_microservice.service;

import com.visor.test_microservice.entity.FileAttachment;
import com.visor.test_microservice.exception.InvalidFileException;
import com.visor.test_microservice.exception.ResourceNotFoundException;
import com.visor.test_microservice.repository.FileAttachmentRepository;
import com.visor.test_microservice.repository.TestRepository;
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
public class FileAttachmentServiceTest {

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private FileAttachmentService fileAttachmentService;

    @Nested
    @DisplayName("Create FileAttachment")
    class CreateFileAttachmentTests {

        @Test
        @DisplayName("should save FileAttachment when testId valid")
        void createFileAttachment_shouldSave_whenValid() throws IOException {
            MultipartFile file = new MockMultipartFile("file", "document.pdf", "pdf", new byte[1]);
            given(testRepository.existsByIdAndDeletedAtIsNull("valid-test-id")).willReturn(true);
            given(fileAttachmentRepository.save(any(FileAttachment.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(s3Service.uploadFile(file)).willReturn("http://example.com/document.pdf");

            FileAttachment result = fileAttachmentService.saveFileAttachment(file, "valid-test-id");

            assertThat(result).isNotNull();
            assertThat(result.getFileUrl()).isEqualTo("http://example.com/document.pdf");
            assertThat(result.getTestId()).isEqualTo("valid-test-id");
            verify(fileAttachmentRepository).save(any(FileAttachment.class));
        }

        @Test
        @DisplayName("should throw when test not found")
        void createFileAttachment_shouldThrow_whenTestNotFound() {
            MultipartFile file = new MockMultipartFile("file", "document.pdf", "pdf", new byte[1]);
            given(testRepository.existsByIdAndDeletedAtIsNull("missing-test-id")).willReturn(false);

            assertThatThrownBy(() -> fileAttachmentService.saveFileAttachment(file, "missing-test-id"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active Test found with ID: missing-test-id");
        }

        @Test
        @DisplayName("should throw when file is empty")
        void createFileAttachment_shouldThrow_whenFileIsEmpty() {
            MultipartFile file = new MockMultipartFile("file", "", "pdf", new byte[0]);
            given(testRepository.existsByIdAndDeletedAtIsNull("valid-test-id")).willReturn(true);

            assertThatThrownBy(() -> fileAttachmentService.saveFileAttachment(file, "valid-test-id"))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("The file is empty or null");
        }

        @Test
        @DisplayName("should throw when file upload fails")
        void createFileAttachment_shouldThrow_whenFileUploadFails() throws IOException {
            MultipartFile file = new MockMultipartFile("file", "document.pdf", "pdf", new byte[1]);
            given(testRepository.existsByIdAndDeletedAtIsNull("valid-test-id")).willReturn(true);
            given(s3Service.uploadFile(file)).willThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> fileAttachmentService.saveFileAttachment(file, "valid-test-id"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error uploading file to S3: Upload failed");
        }

    }

    @Nested
    @DisplayName("Get FileAttachment by Test ID")
    class GetFileAttachmentByTestIdTests {

        @Test
        @DisplayName("should return FileAttachment when Test exists")
        void getFileAttachmentByTestId_shouldReturn_whenExists() {
            String id = "valid-test-id";
            FileAttachment attachment = new FileAttachment();
            attachment.setTestId(id);

            given(testRepository.existsByIdAndDeletedAtIsNull(id)).willReturn(true);
            given(fileAttachmentRepository.findByTestIdAndDeletedAtIsNull(id)).willReturn(List.of(attachment));

            List<FileAttachment> result = fileAttachmentService.getAttachmentsByTestEntityId(id);

            assertThat(result).isNotEmpty().hasSize(1).extracting(FileAttachment::getTestId).containsExactly(id);
            verify(fileAttachmentRepository).findByTestIdAndDeletedAtIsNull(id);
        }

        @Test
        @DisplayName("should throw when not found")
        void getFileAttachmentByTestId_shouldThrow_whenNotFound() {
            String id = "missing-test-id";
            given(testRepository.existsByIdAndDeletedAtIsNull(id)).willReturn(false);

            assertThatThrownBy(() -> fileAttachmentService.getAttachmentsByTestEntityId(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active Test found with ID: " + id);
        }
    }

    @Nested
    @DisplayName("Delete FileAttachment")
    class DeleteFileAttachmentTests {

        @Test
        @DisplayName("should set deletedAt when exists")
        void deleteFileAttachment_shouldSetDeletedAt_whenExists() {
            String id = "valid-attachment-id";
            FileAttachment existing = new FileAttachment();
            existing.setTestId(id);

            given(fileAttachmentRepository.findByIdAndDeletedAtIsNull(id)).willReturn(java.util.Optional.of(existing));
            given(fileAttachmentRepository.save(any(FileAttachment.class))).willAnswer(invocation -> invocation.getArgument(0));

            fileAttachmentService.deleteFileAttachment(id);

            assertThat(existing.getDeletedAt()).isNotNull();
            verify(fileAttachmentRepository).save(existing);
        }

        @Test
        @DisplayName("should throw when not found")
        void deleteFileAttachment_shouldThrow_whenNotFound() {
            String id = "missing-attachment-id";
            given(fileAttachmentRepository.findByIdAndDeletedAtIsNull(id)).willReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> fileAttachmentService.deleteFileAttachment(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No active attachment found with ID: " + id);
        }
    }

}
