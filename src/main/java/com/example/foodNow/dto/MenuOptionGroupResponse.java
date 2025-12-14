package com.example.foodNow.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuOptionGroupResponse {
    private Long id;
    private String name;
    private boolean isRequired;
    private boolean isMultiple;
    private List<MenuOptionResponse> options;
}
