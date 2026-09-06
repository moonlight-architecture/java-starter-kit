package com.servicecops.project.models.database;

import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "system_permission", schema = "public", catalog = "project_db")
public class SystemPermissionModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "permission_code")
    private String permissionCode;
    @Basic
    @Column(name = "permission_name")
    private String permissionName;
    @Basic
    @Column(name = "domain")
    @Enumerated(EnumType.STRING)
    private AppDomains permissionDomain;
}
