package com.example.foodNow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class MenuOptionGroupRequest {

    private Long id;

    @NotBlank(message = "Group name is required")
    private String name;

    private boolean isRequired;

    private boolean isMultiple;

    @Valid
    private List<MenuOptionRequest> options;
}
