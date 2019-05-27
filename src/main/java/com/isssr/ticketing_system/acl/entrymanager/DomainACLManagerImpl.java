package com.isssr.ticketing_system.acl.entrymanager;

import com.isssr.ticketing_system.rest.acl.PermissionDTO;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;

@Component
public class DomainACLManagerImpl implements DomainACLManager {

    private static final Logger log = LoggerFactory.getLogger(DomainACLManagerImpl.class);

    @Resource
    //MutableAclService aclService;
            MyJDBCMutableACLService aclService;

    @LogOperation(inputArgs = {"identity"}, returnObject = true, tag = "ACL", opName = "createOrRetrieveACL")
    private MutableAcl createOrRetrieveACL(ObjectIdentity identity) {
        MutableAcl acl;
        try {
            acl = (MutableAcl) aclService.readAclById(identity);
        } catch (NotFoundException e) {
            acl = aclService.createAcl(identity);
        }
        return acl;
    }

    private void isPermissionGranted(Permission domainPermission, Sid sid, MutableAcl acl) {
        try {
            acl.isGranted(Arrays.asList(domainPermission), Arrays.asList(sid), false);
            /*
            if (!acl.isGranted(Arrays.asList(domainPermission), Arrays.asList(sid), false)) {
                acl.updateAce(acl.getEntries().size(), domainPermission);
            }
            */
        } catch (NotFoundException e) {
            acl.insertAce(acl.getEntries().size(), domainPermission, sid, true);
        }
    }

    private void denyPermission(Permission domainPermission, Sid sid, MutableAcl acl) {
        try {
            acl.isGranted(Arrays.asList(domainPermission), Arrays.asList(sid), false);
            /*
            if (acl.isGranted(Arrays.asList(domainPermission), Arrays.asList(sid), false)) {
                acl.updateAce(acl.getEntries().size(), domainPermission);
            }
            */
        } catch (NotFoundException e) {
            acl.insertAce(acl.getEntries().size(), domainPermission, sid, false);
        }
    }

    @Override
    @LogOperation(inputArgs = {"clazz", "identifier", "sid", "domainPermission"}, tag = "ACL", opName = "addPermission")
    public <T> void addPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);
        //deleteAcl(identity);
        MutableAcl acl = createOrRetrieveACL(identity);
        isPermissionGranted(domainPermission, sid, acl);
        aclService.updateAcl(acl);

    }

    @Override
    public <T> void denyPermission(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz, identifier);
        //deleteAcl(identity);
        MutableAcl acl = createOrRetrieveACL(identity);
        denyPermission(domainPermission, sid, acl);
        aclService.updateAcl(acl);

    }

    @Override
    public <T> void removePermission(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
        MutableAcl acl = (MutableAcl) aclService.readAclById(identity);

        AccessControlEntry[] entries = acl.getEntries().toArray(new AccessControlEntry[acl.getEntries().size()]);
        for (int i = 0; i < acl.getEntries().size(); i++) {
            if (entries[i].getSid().equals(sid) && entries[i].getPermission().equals(domainPermission)) {
                acl.deleteAce(i);
            }
        }
        aclService.updateAcl(acl);
    }

    @Override
    public <T> boolean isPermissionGranted(Class<T> clazz, Serializable identifier, Sid sid, Permission domainPermission) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
        MutableAcl acl = (MutableAcl) aclService.readAclById(identity);
        boolean isGranted = false;
        try {
            isGranted = acl.isGranted(Arrays.asList(domainPermission), Arrays.asList(sid), false);
        } catch (NotFoundException e) {
            log.info("Unable to find an ACE for the given object", e);
        } catch (UnloadedSidException e) {
            log.error("Unloaded Sid", e);
        }
        return isGranted;
    }

    @Override
    public <T> void deleteAllACL(Class<T> clazz, Serializable identifier) {
        ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
        aclService.deleteAcl(identity, true);
    }

    @Override
    public <T> Map<Sid, List<PermissionDTO>> getAllPermissionOnDomainModel(Class<T> clazz, Serializable identifier) {
        Map<Sid, List<PermissionDTO>> perms = new HashMap<>();
        List<PermissionDTO> p = null;

        ObjectIdentity identity = new ObjectIdentityImpl(clazz.getCanonicalName(), identifier);
        MutableAcl acl = (MutableAcl) aclService.readAclById(identity);

        List<AccessControlEntry> entries = acl.getEntries();
        for (AccessControlEntry ace : entries) {
            Sid sid = ace.getSid();
            p = perms.get(sid);
            if (p == null) {
                p = new ArrayList<>();
            }

            p.add(new PermissionDTO(ace.getPermission().getMask(), ace.isGranting()));
            perms.put(sid, p);
        }

        return perms;
    }

    @Override
    public <T> void createSidForUser(String username) {

        aclService.createOrRetrieveSidPrimaryKey(username,true,true);

    }

    /*
    public void deleteAcl(ObjectIdentity objectIdentity) {
        aclService.deleteAcl(objectIdentity, true);
    }
    */

    /*
    @Transactional
    public void grantPermission(String principal, T entity, DomainPermission[] customPermissions) {

        ObjectIdentity oi = new ObjectIdentityImpl(entity.getClass(), entity.getId());
        //Sid sid = new PrincipalSid(principal); per username
        Sid sid = new GrantedAuthoritySid(principal); // per ruolo

        MutableAcl acl = createOrRetrieveACL(oi);

        for (DomainPermission customPermission : customPermissions) {
            switch (customPermission) {
                case READ:
                    acl.insertAce(acl.getEntries().size(),BasePermission.READ,sid, true);
                    break;
                case WRITE:
                    acl.insertAce(acl.getEntries().size(),BasePermission.WRITE,sid, true);
                    break;
            }
        }

        aclService.updateAcl(acl);
    }
    */
}
