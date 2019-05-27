package com.isssr.ticketing_system.acl.entrymanager;

import com.isssr.ticketing_system.rest.acl.PermissionDTO;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface DomainACLManager {
    /**
     * @param clazz            Class to control with ACL
     * @param identifier       Id of the class instance
     * @param sid              Secure identity
     * @param domainPermission Permission to grant
     * @param <T>
     */
    <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission);

    <T> void denyPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission);

    <T> void removePermission(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission);

    <T> boolean isPermissionGranted(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission);

    <T> void deleteAllACL(Class<T> clazz, Serializable identifier);

    <T> Map<Sid, List<PermissionDTO>> getAllPermissionOnDomainModel(Class<T> clazz, Serializable identifier);

    <T> void createSidForUser(String username);

    //<T> void deleteSidForUser(String username);

}
