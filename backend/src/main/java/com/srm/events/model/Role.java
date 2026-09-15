package com.srm.events.model;

import java.time.LocalDateTime;

public class Role {

    private Long roleId;
    private String roleName;
    private LocalDateTime createdAt;

    public Role() {
    }

    public Role(Long roleId, String roleName, LocalDateTime createdAt) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.createdAt = createdAt;
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
