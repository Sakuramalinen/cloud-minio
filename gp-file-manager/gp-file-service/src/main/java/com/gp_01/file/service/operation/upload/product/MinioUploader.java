package com.gp_01.file.service.operation.upload.product;

import com.gp_01.file.service.operation.upload.Uploader;
import com.gp_01.file.service.operation.upload.domain.UploadFile;
//import com.gp_01.file.util.FileUtils;
import com.gp_01.file.service.operation.upload.domain.UploadFileResult;
import com.gp_01.file.service.util.MinioUtils;
import com.gp_01.file.service.util.RedisUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//@RequiredArgsConstructor
//@Component("minio_uploader")
@Component
public class MinioUploader extends Uploader {

    private final RedisUtils redisUtils;

    @Autowired
    private  MinioUtils minioUtils;

    public MinioUploader(RedisUtils redisUtils, RedissonClient redissonClient) {
        super(redisUtils, redissonClient);
        this.redisUtils = redisUtils;
    }




    @Override
    public UploadFileResult uploadFileChunk(UploadFile uploadFile, MultipartFile multipartFile) {

        String basePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        //查看该切片是否上传过
        if (!getUploadChunkStatusFromCache(uploadFile)) {

            minioUtils.uploadChunk(multipartFile,uploadFile.getFileMd5(), uploadFile.getCurrentChunkIndex(), basePath);
            //写切片状态
            writeUploadChunkStatusToCache(uploadFile);

        }

        //判断是否上传完成
        Long progress = getUploadStatusFromCache(uploadFile.getFileMd5());
        boolean isComplete = progress.equals(uploadFile.getChunkNumber());

        if(isComplete){
            //合并切片
            String extendName = uploadFile.getFileName().substring(uploadFile.getFileName().lastIndexOf('.'));
            String targetPath = "original/" + basePath + "/" + uploadFile.getFileMd5() + extendName;
            minioUtils.mergeFile(uploadFile.getChunkNumber(), basePath, uploadFile.getFileMd5(), targetPath);
            //删除临时文件
            minioUtils.deleteChunk(uploadFile.getChunkNumber(), basePath, uploadFile.getFileMd5());
            //删除进度缓存
            deleteUploadCHunkStatusToCache(uploadFile);
        }
        UploadFileResult result = new UploadFileResult();
        result.setProgress(progress);
        result.setUploaded(isComplete);
        return result;
    }


}
