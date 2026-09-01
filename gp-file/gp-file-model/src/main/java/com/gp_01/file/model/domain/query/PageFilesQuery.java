package com.gp_01.file.model.domain.query;

import com.gp_01.common.domain.query.PageParams;
import com.gp_01.common.enums.FileTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageFilesQuery extends PageParams {

    private Long parentId;

}
