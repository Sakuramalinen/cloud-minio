package com.gp_01.file.service.constants;

public interface RabbitmqFileConstants {
    //============================交换机============================
    //文件服务交换机
    String EXCHANGE_TOPIC_FILE = "exchange_topic_file";




    //============================routing_key============================
    //保存上传文件进度routing_key
    String RK_UPLOAD_PROGRESS_SAVE = "file.uploadProgress.save";




    //============================queue============================
    //保存上传文件进度queue
    String QUEUE_UPLOAD_PROGRESS_SAVE = "queue_file_uploadTaskRecordService_save";

    
}
