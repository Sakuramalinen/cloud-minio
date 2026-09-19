package com.gp_01.file.service.oss.delete;

import java.util.List;

public interface Deleter {

    void deleteObjects(List<String> objectPaths);
}
