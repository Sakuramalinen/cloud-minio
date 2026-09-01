package com.gp_01.file.service.constants;

public interface RabbitmqFileConstants {
    //============================交换机============================
    //文件服务交换机
    String EXCHANGE_TOPIC_FILE = "exchange.topic.file";




    //保存上传文件进度
    String QUEUE_UPLOAD_PROGRESS_SAVE = "queue.file_uploadTaskRecordService_save";
    String RK_UPLOAD_PROGRESS_SAVE = "file.uploadProgress_save";


    //上传文件进行后期处理
    String QUEUE_UPLOAD_POST_PROCESS = "queue.file_uploadPostProcess";
    String RK_UPLOAD_POST_PROCESS = "file.uploadPostProcess";

    //死信
    String DLX_QUEUE_FILE = "dlx.queue.file";
    String DLX_RK_FILE = "dlx.file";

    //错误
    String ERROR_QUEUE_FILE = "error.queue.file";
    String ERROR_RK_FILE = "error.file";

    //增加已使用空间大小
    String QUEUE_INCREMENT_USE_RESTORE = "queue.file_incrementUseRestore";
    String RK_INCREMENT_USE_RESTORE = "file.incrementUseRestore";




    
}
