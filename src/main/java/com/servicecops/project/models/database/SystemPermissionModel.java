package com.servicecops.project.models.database;

import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "system_permission")
public class SystemPermissionModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "permission_code")
    private String permissionCode;
    @Column(name = "permission_name")
    private String permissionName;
    @Column(name = "domain")
    @Enumerated(EnumType.STRING)
    private AppDomains permissionDomain;
}
