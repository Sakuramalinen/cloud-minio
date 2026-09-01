package com.gp_01.file.model.domain.dto.userFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateRootDTO {

    @NotNull
    private Long userId;
}
