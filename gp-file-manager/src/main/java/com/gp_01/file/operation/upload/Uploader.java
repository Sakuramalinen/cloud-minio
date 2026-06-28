package com.gp_01.file.operation.upload;

import com.gp_01.common.exception.FileNotFoundException;
import com.gp_01.common.exception.UploadFileChunkIndexException;
import com.gp_01.file.operation.upload.domain.UploadFile;
import com.gp_01.file.operation.upload.domain.UploadFileResult;
import com.gp_01.file.util.RedisUtils;
import io.minio.messages.Upload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Slf4j
public abstract class Uploader {

    private final RedisUtils redisUtils;
    private final RedissonClient redissonClient;

    public Boolean getUploadStatusFromFile(File progressFile) {
        try (RandomAccessFile raf = new RandomAccessFile(progressFile, "rw")) {
            byte[] fileArray = FileUtils.readFileToByteArray(progressFile);
            for (byte b : fileArray) {
                if (b != Byte.MAX_VALUE) {
                    raf.close();
                    return false;
                }
            }
            boolean deleted = progressFile.delete();
            return true;
        } catch (IOException e) {
            throw new FileNotFoundException("上传进度文件不存在");
        }
    }

    public void writeStatusToFile(UploadFile uploadFile, File progressFile) {
        try (RandomAccessFile raf = new RandomAccessFile(progressFile, "rw")) {
            //设置文件大小
            raf.setLength(uploadFile.getChunkNumber());
            //设置偏移量
            raf.seek(uploadFile.getCurrentChunkIndex() - 1);
            //写进度标记
            raf.write(Byte.MAX_VALUE);
        } catch (IOException e) {
            throw new FileNotFoundException("上传文件写进度失败");
        }
    }

    public Long getUploadStatusFromCache(String identifier){
        String key = "gp_01:file_manager:upload_file:status:" + identifier;
        return redisUtils.countBitMap(key);
    }
    public Boolean getUploadChunkStatusFromCache(UploadFile uploadFile){
        String key = "gp_01:file_manager:upload_file:status:" + uploadFile.getFileMd5();
        return redisUtils.getBitMap(key, uploadFile.getCurrentChunkIndex() - 1);
    }
    public void writeUploadChunkStatusToCache(UploadFile uploadFile){
        String key = "gp_01:file_manager:upload_file:status:" + uploadFile.getFileMd5();
        redisUtils.setBitMap(key,uploadFile.getCurrentChunkIndex() - 1, true);
    }

    public void deleteUploadCHunkStatusToCache(UploadFile uploadFile){
        String key = "gp_01:file_manager:upload_file:status:" + uploadFile.getFileMd5();
        redisUtils.deletedKey(key);
    }


    public void rectifier(UploadFile uploadFile, MultipartFile multipartFile) {
        int serviceTotal = 3;

        String key = "gp_01:file_manager:upload_file:rectifier:file_md5:" + uploadFile.getFileMd5();
        String current_upload_chunk_index = "gp_01:file_manager:upload_file:rectifier:chunk_index:" + uploadFile.getCurrentChunkIndex();
        RLock lock = redissonClient.getLock(key);
        try {
            boolean acquired = lock.tryLock(300, TimeUnit.SECONDS);
            if (!acquired) {
                log.error("上传文件切片，获取分布式锁超时");
                throw new UploadFileChunkIndexException("服务器超时");
            }
            //如果内存没有分片就把分片一号添加进去
            if (redisUtils.get(current_upload_chunk_index) == null) {
                redisUtils.set(current_upload_chunk_index, "1", 1000 * 60 * 60L);
            }
            int chunkIndex = Integer.parseInt(redisUtils.get(current_upload_chunk_index));
            if (chunkIndex != uploadFile.getCurrentChunkIndex()) {
                lock.unlock();
                Thread.sleep(100);
                while (lock.tryLock(300, TimeUnit.SECONDS)) {

                    chunkIndex = Integer.parseInt(redisUtils.get(current_upload_chunk_index));

                    if (uploadFile.getCurrentChunkIndex() <= chunkIndex) {
                        break;
                    } else {
                        if (Math.abs(uploadFile.getCurrentChunkIndex() - chunkIndex) > serviceTotal) {
                            log.error("当前传入的编号为：{}, 正确传入的编号为：{}", uploadFile.getCurrentChunkIndex(), chunkIndex);
                            throw new UploadFileChunkIndexException("传入切片异常");
                        }
                        lock.unlock();
                        Thread.sleep(100);
                    }
                }
            }
            if (uploadFile.getCurrentChunkIndex() == chunkIndex) {
                //执行上传切片功能
                uploadFileChunk(uploadFile, multipartFile);
                log.debug("上传文件：{},第{}个切片成功", uploadFile.getFileMd5(), uploadFile.getCurrentChunkIndex());
                //切片编号+1
                redisUtils.increment(current_upload_chunk_index);
            }
        } catch (Exception e) {
            throw new UploadFileChunkIndexException(e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public abstract UploadFileResult uploadFileChunk(UploadFile uploadFile, MultipartFile multipartFile);

    public void execute(UploadFile uploadFile, MultipartFile multipartFile){
        rectifier(uploadFile,multipartFile);
    }

}
