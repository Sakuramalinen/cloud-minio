package com.gp_01.file.service.oss.impl;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.StorageException;
import com.gp_01.file.service.oss.FileManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class S3FileManipulator implements FileManipulator {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;


    @Override
    public void standardUpload(byte[] fileBytes, String bucketName, String objectKey, String contentType) {
        try {
            //上传
            s3Client.putObject(
                    r -> r.bucket(bucketName).key(objectKey).contentType(contentType).contentLength((long) fileBytes.length),
                    RequestBody.fromBytes(fileBytes)
            );
            log.debug("Standard upload successfully. object-key -> {}", objectKey);
        } catch (Exception e) {
            log.error("Error upload: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);
        }
    }

    @Override
    public String initiateMultipartUpload(String bucketName, String objectKey, String contentType) {
        try {

            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                    r -> r.bucket(bucketName).key(objectKey).contentType(contentType)
            );
            String uploadId = response.uploadId();
            log.debug("Initiate multipart upload successfully. object-key -> {}", objectKey);
            return uploadId;
        } catch (Exception e) {
            log.error("Error initiating multipart update: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);
        }
    }

    @Override
    public String completeMultipartUpload(String bucketName, String objectKey, String uploadId, List<CompletedPart> completedParts) {
        try {

            if (completedParts == null || completedParts.isEmpty()) {
                return null;
            }

            CompleteMultipartUploadResponse completeMultipartUploadResponse = s3Client.completeMultipartUpload(
                    r -> r.bucket(bucketName).key(objectKey).uploadId(uploadId).multipartUpload(
                            c -> c.parts(completedParts)
                    )
            );
            log.debug("Upload completed successfully. object-key -> {}", objectKey);
            return completeMultipartUploadResponse.eTag();
        } catch (Exception e) {
            log.error("Error completing multipart upload: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);
        }
    }

    public InputStream obtainDownloadInputStream(String bucketName, String objectKey, String contentType, String fileName) {
        try {
            String contentDisposition = ContentDisposition.attachment().filename(fileName).build().toString();

            ResponseInputStream<GetObjectResponse> getObjectResponseResponseInputStream = s3Client.getObject(
                    r -> r.bucket(bucketName).key(objectKey).responseContentDisposition(contentDisposition).responseContentType(contentType)
            );
            log.debug("obtain download inputStream successfully. object-key -> {}", objectKey);
            return getObjectResponseResponseInputStream;
        } catch (Exception e){
            log.error("Error obtain download inputStream: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR);
        }
    }

    @Override
    public String generateUploadPreSignedUrl(String bucketName, String objectKey, String contentType, Long contentLength, Duration expiration) {
        try {

            PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
                    r -> r.signatureDuration(expiration).putObjectRequest(
                            p -> p.bucket(bucketName).key(objectKey).contentType(contentType).contentLength(contentLength)
                    )
            );
            String url = presignedPutObjectRequest.url().toString();
            log.debug("Generate upload pre signed url successfully. object-key -> {}", objectKey);
            return url;
        } catch (Exception e) {
            log.error("Error generation upload pre signed url: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);
        }
    }

    @Override
    public String generateUploadPartPreSignedUrl(String bucketName, String objectKey, String uploadId, Integer partNumber, Duration expiration) {
        try {

            PresignedUploadPartRequest presignedUploadPartRequest = s3Presigner.presignUploadPart(
                    r -> r.signatureDuration(expiration).uploadPartRequest(
                            u -> u.bucket(bucketName).uploadId(uploadId).partNumber(partNumber).key(objectKey)
                    )
            );
            String url = presignedUploadPartRequest.url().toString();
            log.debug("Generate upload part pre signed url successfully. object-key -> {}", objectKey);
            return url;
        } catch (Exception e) {
            log.error("Error generation upload part pre signed url: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);
        }
    }

    @Override
    public String generateDownloadPreSignedUrl(String bucketName, String objectKey, String fileName, String contentType, Duration expiration) {
        try {

            String contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                    r -> r.signatureDuration(expiration).getObjectRequest(
                            g -> g.bucket(bucketName).key(objectKey).responseContentType(contentType).responseContentDisposition(contentDisposition)
                    )
            );

            String url = presignedGetObjectRequest.url().toString();

            log.debug("Generate download pre sign url successfully. object-key -> {}", objectKey);
            return url;
        } catch (Exception e) {
            log.error("Error generation download pre sign url: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);

        }
    }

    @Override
    public String generatePreviewPreSignedUrl(String bucketName, String objectKey, String contentType, Duration expiration) {
        try {

            String contentDisposition = ContentDisposition.inline().build().toString();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                    r -> r.signatureDuration(expiration).getObjectRequest(
                            g -> g.bucket(bucketName).key(objectKey).responseContentType(contentType).responseContentDisposition(contentDisposition)
                    )
            );

            String url = presignedGetObjectRequest.url().toString();

            log.debug("generate preview pre signed url successfully. object-key -> {}", objectKey);
            return url;
        } catch (Exception e) {
            log.error("Error generating preview pre signed url: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);

        }
    }

    @Override
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
        try {

            s3Client.abortMultipartUpload(
                    r -> r.bucket(bucketName).uploadId(uploadId).key(objectKey)
            );
            log.debug("Multipart upload aborted successfully");
        } catch (Exception e) {
            log.error("Error aborting multipart upload: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);

        }
    }

    @Override
    //TODO 最多支持获取1000分片
    public List<Part> listParts(String bucketName, String objectKey, String uploadId) {
        try {

            ListPartsResponse listPartsResponse = s3Client.listParts(
                    r -> r.bucket(bucketName).key(objectKey).uploadId(uploadId)
            );

            List<Part> parts = listPartsResponse.parts();

            log.debug("List upload parts successfully. object-key -> {}", objectKey);
            return parts;
        } catch (Exception e) {
            log.error("Error Listing uploaded parts: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);

        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {

            s3Client.deleteObject(
                    r -> r.bucket(bucketName).key(objectKey)
            );

            log.debug("Delete object successfully. object-key -> {}", objectKey);
        } catch (Exception e) {
            log.error("Error Deleting object: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);

        }
    }

    @Override
    public void deleteObjectBatch(String bucketName, List<String> objectKeys) {
        try {

            if (objectKeys == null || objectKeys.isEmpty()) {
                return;
            }

            ArrayList<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
            for (String objectKey : objectKeys) {
                ObjectIdentifier objectIdentifier = ObjectIdentifier.builder()
                        .key(objectKey)
                        .build();
                objectIdentifiers.add(objectIdentifier);
            }

            s3Client.deleteObjects(
                    r -> r.bucket(bucketName).delete(
                            d -> d.objects(objectIdentifiers).quiet(true)
                    )
            );
            log.debug("Delete objects batch successfully. object-keys -> {}", objectKeys);
        } catch (Exception e) {
            log.error("Error deleting objects batch: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);

        }
    }



    public HeadObjectResponse obtainObjectMetaData(String bucketName, String objectKey){
        try{
            HeadObjectResponse headObjectResponse = s3Client.headObject(
                    r -> r.bucket(bucketName).key(objectKey)
            );
            log.debug("obtain object metadata successfully. object-key -> {}", objectKey);
            return headObjectResponse;
        }catch (Exception e){
            log.error("Error obtain objects metadata: {}", e.getMessage());
            throw new StorageException(ErrorCode.OSS_ERROR, e);
        }
    }
}
