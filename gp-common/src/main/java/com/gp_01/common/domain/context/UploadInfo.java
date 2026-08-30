package com.gp_01.common.domain.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadInfo {

    String uploadId;

    String objectPath;

//    List<Integer> chunkNumbers;

}
