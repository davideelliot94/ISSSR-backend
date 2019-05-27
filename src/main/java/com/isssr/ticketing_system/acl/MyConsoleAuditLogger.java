package com.isssr.ticketing_system.acl;

import com.isssr.ticketing_system.configuration.ConfigProperties;
import com.isssr.ticketing_system.entity.ACLRecord;
import com.isssr.ticketing_system.controller.ACLRecordController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AuditableAccessControlEntry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

import javax.transaction.Transactional;


@Controller
public class MyConsoleAuditLogger implements AuditLogger {

    private ACLRecord record;

    private final ACLRecordController aclRecordController;
    private final ConfigProperties configProperties;

    @Autowired
    public MyConsoleAuditLogger(@Qualifier(value = "ACLRecordController") ACLRecordController aclRecordController, ConfigProperties configProperties) {
        this.aclRecordController = aclRecordController;
        this.configProperties = configProperties;
    }

    @Override
    @Transactional
    public void logIfNeeded(boolean granted, AccessControlEntry ace) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getName();

        Assert.notNull(ace, "AccessControlEntry required");
        if (ace instanceof AuditableAccessControlEntry) {
            AuditableAccessControlEntry auditableAce = (AuditableAccessControlEntry) ace;
            if (granted && auditableAce.isAuditSuccess()) {
                if (configProperties.getDebugConfig().isDebug()) {
                    System.out.println("GRANTED due to ACE: " + ace);
                    System.out.println("ID: " + ace.getId());
                    System.out.println("SID: " + ace.getSid());
                    System.out.println("ACL: " + ace.getAcl().getObjectIdentity().getType());
                    System.out.println("ACL: " + ace.getAcl().getObjectIdentity().getIdentifier());
                    System.out.println("Permission: " + ace.getPermission().getPattern());
                    System.out.println("Permission: " + ace.getPermission().getMask());
                    System.out.println("IsGranting: " + ace.isGranting());
                    System.out.println("isAuditFailure: " + ((AuditableAccessControlEntry) ace).isAuditFailure());
                    System.out.println("isAuditSuccess: " + ((AuditableAccessControlEntry) ace).isAuditSuccess());
                }


                record = new ACLRecord();

                record.setSid(principal);
                record.setType(ace.getAcl().getObjectIdentity().getType());
                record.setType_id(Integer.valueOf(ace.getAcl().getObjectIdentity().getIdentifier().toString()));
                record.setPermission(ace.getPermission().getMask());
                record.setGranting(ace.isGranting());


                synchronized (record) {
                    aclRecordController.saveRecord(record);
                }



            } else if (!granted && auditableAce.isAuditFailure()) {
                if (configProperties.getDebugConfig().isDebug()) {
                    System.out.println("DENIED due to ACE: " + ace);
                    System.out.println("ID: " + ace.getId());
                    System.out.println("SID: " + ace.getSid());
                    System.out.println("ACL: " + ace.getAcl().getObjectIdentity().getType());
                    System.out.println("ACL: " + ace.getAcl().getObjectIdentity().getIdentifier());
                    System.out.println("Permission: " + ace.getPermission().getPattern());
                    System.out.println("Permission: " + ace.getPermission().getMask());
                    System.out.println("IsGranting: " + ace.isGranting());
                    System.out.println("isAuditFailure: " + ((AuditableAccessControlEntry) ace).isAuditFailure());
                    System.out.println("isAuditSuccess: " + ((AuditableAccessControlEntry) ace).isAuditSuccess());
                }


                record = new ACLRecord();

                record.setSid(principal);
                record.setType(ace.getAcl().getObjectIdentity().getType());
                record.setType_id(Integer.valueOf(ace.getAcl().getObjectIdentity().getIdentifier().toString()));
                record.setPermission(ace.getPermission().getMask());
                record.setGranting(ace.isGranting());

                synchronized (record) {
                    aclRecordController.saveRecord(record);
                }
            }
        }

    }

}
