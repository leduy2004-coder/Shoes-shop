package com.java.file_service.service;

import com.java.file_service.config.properties.MinioProperties;
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
    private final MinioProperties minioProperties;

    @SneakyThrows
    public String upload(@NonNull final MultipartFile file, String fileName) {
        log.info("Bucket: {}, file size: {}", BUCKET, file.getSize());
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(fileName)
                            .contentType(Objects.isNull(file.getContentType()) ? "image/png" : file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Error saving image: {}", ex.getMessage());
            throw new AppException(UPLOAD_FAILED);
        }

        // ✅ Trả về URL public đơn giản (không có chữ ký)
        String publicUrl = String.format("%s/%s/%s",
                minioProperties.getPublicUrl(),
                BUCKET,
                fileName);

        log.info("Generated public URL: {}", publicUrl);
        return publicUrl;
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
