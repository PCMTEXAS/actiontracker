package com.pcmtexas.actiontracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractRequest {

    @NotBlank(message = "Notes content is required")
    private String notes;
}
