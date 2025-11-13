package com.spring.logitrack.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Test
    void testUploadFile_success() throws Exception {

        // mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "hello".getBytes()
        );

        // Inject value for @Value
        Field field = S3Service.class.getDeclaredField("bucketName");
        field.setAccessible(true);
        field.set(s3Service, "my-test-bucket");

        // mock response
        PutObjectResponse response = PutObjectResponse.builder().build();

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(response);  // <-- CORRECT

        // execute
        String url = s3Service.uploadFile(file);

        // verify S3 upload was called once
        verify(s3Client, times(1)).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );

        assert url.startsWith("https://my-test-bucket.s3.amazonaws.com/uploads/");
    }
}

