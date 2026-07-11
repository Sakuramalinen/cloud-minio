package com.gp_01.file.service.operation.upload;

import com.gp_01.file.service.constants.RedisConstants;
import com.gp_01.file.service.operation.upload.domain.UploadFile;
import com.gp_01.file.service.operation.upload.domain.UploadFileResult;
import com.gp_01.file.service.util.RedisUtils;
import io.minio.messages.Upload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.xmlunit.builder.Input;

import java.io.InputStream;


@RequiredArgsConstructor
@Slf4j
public abstract class Uploader {

    private final RedisUtils redisUtils;

    /**
     * 统计已上传切片数量
     */
    private Long getUploadedChunkCount(String fileMd5) {
        String key = RedisConstants.UPLOAD_STATUS_PREFIX + fileMd5;
        return redisUtils.countBitMap(key);
    }

    /**
     * 判断切片是否已上传
     */
    private Boolean isChunkAlreadyUploaded(String fileMd5, Long currentChunkIndex) {
        String key = RedisConstants.UPLOAD_STATUS_PREFIX + fileMd5;
        return redisUtils.getBitMap(key, currentChunkIndex - 1);
    }

    /**
     * 标记切片为已上传
     */
    private void makeChunkUploaded(String fileMd5, Long currentChunkIndex) {
        String key = RedisConstants.UPLOAD_STATUS_PREFIX + fileMd5;
        redisUtils.setBitMap(key, currentChunkIndex - 1, true);
    }

    /**
     * 清除切片进度缓存
     */
    private void clearFileChunkProgressCache(String fileMd5) {
        String key = RedisConstants.UPLOAD_STATUS_PREFIX + fileMd5;
        redisUtils.deletedKey(key);
    }

    /**
     * 默认上传入口
     * @param uploadFile
     * @param inputStream
     * @return
     */
    public UploadFileResult defaultUpload(UploadFile uploadFile, InputStream inputStream){
        if (uploadFile.getIsChunk() || uploadFile.getCurrentChunkSize() == null) {
            return uploadByChunkFile(uploadFile,inputStream);
        } else {
            return uploadBySingleFile(uploadFile, inputStream);
        }
    }

    /**
     * 文件切片上传入口
     * @param uploadFile
     * @param inputStream
     * @return
     */
    public UploadFileResult uploadByChunkFile(UploadFile uploadFile, InputStream inputStream) {
        String tempBucketName = "temp";
        //查看该切片是否上传过
        if (!isChunkAlreadyUploaded(uploadFile.getFileMd5(), uploadFile.getCurrentChunkIndex())) {

            uploadChunk(uploadFile, tempBucketName, inputStream);
            //写切片状态
            makeChunkUploaded(uploadFile.getFileMd5(), uploadFile.getCurrentChunkIndex());
        }

        //判断是否上传完成
        Long progress = getUploadedChunkCount(uploadFile.getFileMd5());
        boolean isComplete = progress.equals(uploadFile.getChunkNumber());

        UploadFileResult result = new UploadFileResult();
        if (isComplete) {
            try {
                //合并切片
                mergeAllChunks(uploadFile, tempBucketName);
            } finally {
                //删除临时切片文件
                deleteAllTempChunks(uploadFile, tempBucketName);
                //删除进度缓存
                clearFileChunkProgressCache(uploadFile.getFileMd5());
            }
        }
        result.setProgress(progress);
        result.setUploaded(isComplete);
        return result;
    }

    /**
     * 文件直接上传入口
     * @param uploadFile
     * @param inputStream
     * @return
     */
    public abstract UploadFileResult uploadBySingleFile(UploadFile uploadFile, InputStream inputStream);

    /*
     * 上传切片
     */
    public abstract void uploadChunk(UploadFile uploadFile, String tempBucketName, InputStream inputStream);

    /**
     * 合并所有切片
     */
    public abstract void mergeAllChunks(UploadFile uploadFile, String tempBucketName);

    /**
     * 删除所有切片
     */
    public abstract void deleteAllTempChunks(UploadFile uploadFile, String tempBucketName);

    /**
     * 上传缩略图
     */
//    public abstract UploadFileResult uploadThumbnailFile(UploadFile uploadFile, InputStream inputStream);
//
//    public abstract UploadFileResult uploadThumbnailFile(UploadFile uploadFile);

}
