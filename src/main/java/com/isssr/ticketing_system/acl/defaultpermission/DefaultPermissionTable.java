package com.isssr.ticketing_system.acl.defaultpermission;

import com.isssr.ticketing_system.acl.Authority;
import com.isssr.ticketing_system.acl.AuthorityName;
import com.isssr.ticketing_system.acl.entrymanager.DomainACLManager;
//import com.uniroma2.isssrbackend.acl.Authority;
//import com.uniroma2.isssrbackend.acl.AuthorityName;
//import com.uniroma2.isssrbackend.acl.entrymanager.DomainACLManager;
//import com.uniroma2.isssrbackend.dao.AuthorityDAO;
import com.isssr.ticketing_system.dao.AuthorityDAO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public abstract class DefaultPermissionTable {

    @Autowired
    private DomainACLManager domainACLManager;

    @Autowired
    private AuthorityDAO authorityDAO;

    protected Map<AuthorityName, List<Permission>> grantedMap;
    protected Map<AuthorityName, List<Permission>> deniedMap;

    private Class domainClass;

    public DefaultPermissionTable(Class domainClass) {
        this.domainClass = domainClass;
        this.grantedMap = new HashMap<>();
        this.deniedMap = new HashMap<>();
        populateMaps();
    }

    public void grantDefaultPermission(Long Id) {

        for (Map.Entry<AuthorityName, List<Permission>> entry : grantedMap.entrySet()) {

            Authority a = authorityDAO.findBySid(entry.getKey());
            Sid sid = a.convertToSid();

            for (Permission p : entry.getValue()) {

                domainACLManager.addPermission(domainClass, Id, sid, p);
            }

        }
    }

    public void denyDefaultPermission(Long Id) {

        for (Map.Entry<AuthorityName, List<Permission>> entry : deniedMap.entrySet()) {

            Authority a = authorityDAO.findBySid(entry.getKey());
            Sid sid = a.convertToSid();

            for (Permission p : entry.getValue()) {

                domainACLManager.denyPermission(domainClass, Id, sid, p);
            }

        }
    }

    protected abstract void populateMaps();

    public void removeDefaultPermission(Long id) {

        domainACLManager.deleteAllACL(domainClass, id);
    }

}
