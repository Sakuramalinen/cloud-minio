package com.gp_01.file.service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp_01.file.model.domain.dto.DownloadAuth;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.service.service.IUploadTaskRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.gp_01.file.service.constants.RabbitmqFileConstants.*;

@Component
@RequiredArgsConstructor
public class RabbitMqListener {

    private final IUploadTaskRecordService uploadTaskRecordService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = QUEUE_UPLOAD_PROGRESS_SAVE),
            exchange = @Exchange(name = EXCHANGE_TOPIC_FILE, type = ExchangeTypes.TOPIC),
            key = {RK_UPLOAD_PROGRESS_SAVE}
    )
    )
    public void uploadProgressSave(UploadProgressSaveDTO dto){
        uploadTaskRecordService.uploadProgressSaveBatch(dto);
    }


}
