package com.java.file_service.service;

import com.java.file_service.exception.AppException;
import com.java.file_service.utility.ConverterUtils;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.java.file_service.exception.ErrorCode.UPLOAD_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private static final String BUCKET = "resources";
    private final MinioClient minioClient;

    @SneakyThrows
    public String upload(@NonNull final MultipartFile file, String fileName) {
        log.info("Bucket: {}, file size: {}", BUCKET, file.getSize());
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .contentType(Objects.isNull(file.getContentType()) ? "image/png; image/jpg;" : file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Error saving image \n {} ", ex.getMessage());
            throw new AppException(UPLOAD_FAILED);
        }
        return minioClient.getPresignedObjectUrl(
                io.minio.GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.GET)
                        .bucket(BUCKET)
                        .object(fileName)
                        .build()
        );
    }

    public byte[] download(String bucket, String name) {
        try (GetObjectResponse inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(name)
                .build())) {
            String contentLength = inputStream.headers().get(HttpHeaders.CONTENT_LENGTH);
            int size = StringUtils.isEmpty(contentLength) ? 0 : Integer.parseInt(contentLength);
            return ConverterUtils.readBytesFromInputStream(inputStream, size);
        } catch (Exception e) {
            throw new AppException(UPLOAD_FAILED);
        }
    }

    @SneakyThrows
    public void delete(@NonNull final String fileName) {
        try {
            log.info("Deleting file: {}", fileName);
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .build()
            );
            log.info("File {} deleted successfully", fileName);
        } catch (Exception ex) {
            log.error("Error deleting file: {}", ex.getMessage());
            throw new AppException(UPLOAD_FAILED);
        }
    }
}
