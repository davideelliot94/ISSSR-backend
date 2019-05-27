package com.isssr.ticketing_system.acl.entrymanager;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

public class DomainPermission extends BasePermission {

    public static final Permission ASSIGN = new DomainPermission(32, 'S');

    protected DomainPermission(int mask) {
        super(mask);
    }

    protected DomainPermission(int mask, char code) {
        super(mask, code);
    }

}
