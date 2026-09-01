package com.gp_01.file.model.domain.dto.taskRecord.listener;

import com.gp_01.file.model.domain.po.UserFile;
import lombok.Data;

import java.util.Collection;

@Data
public class IncrementUseRestoreDTO {

    Collection<UserFile> userFiles;

    Long UserId;

    Boolean isAdd;

}
