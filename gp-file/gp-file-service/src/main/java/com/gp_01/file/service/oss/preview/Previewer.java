package com.gp_01.file.service.oss.preview;

import java.util.concurrent.TimeUnit;

public interface Previewer {

    String previewPreSignUrl(String bucket, String objectPath, String contentType, Integer expiry, TimeUnit unit);
}
