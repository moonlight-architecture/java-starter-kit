package com.servicecops.project.permissions;

import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private String code;
    private String name;
    private AppDomains domain;
    private Boolean shipWithAdmin = false;
    public Permission(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Permission(String code, String name, AppDomains domain) {
        this.code = code;
        this.name = name;
        this.domain = domain;
    }
}
