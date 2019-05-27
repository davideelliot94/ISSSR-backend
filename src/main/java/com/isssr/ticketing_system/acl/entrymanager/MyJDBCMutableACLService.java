package com.isssr.ticketing_system.acl.entrymanager;

import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class MyJDBCMutableACLService extends JdbcAclService implements MutableAclService {
    private static final String DEFAULT_INSERT_INTO_ACL_CLASS = "insert into acl_class (class) values (?)";
    private static final String DEFAULT_INSERT_INTO_ACL_CLASS_WITH_ID = "insert into acl_class (class, class_id_type) values (?, ?)";
    private boolean foreignKeysInDatabase = true;
    private final AclCache aclCache;
    private String deleteEntryByObjectIdentityForeignKey = "delete from acl_entry where acl_object_identity=?";
    private String deleteObjectIdentityByPrimaryKey = "delete from acl_object_identity where id=?";
    //private String classIdentityQuery = "select @@IDENTITY";
    private String classIdentityQuery = "select currval('acl_class_id_seq')";
    //private String sidIdentityQuery = "SELECT @@IDENTITY";
    private String sidIdentityQuery = "select currval('acl_sid_id_seq')";
    private String insertClass = "insert into acl_class (class) values (?)";
    private String insertEntry = "insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)values (?, ?, ?, ?, ?, ?, ?)";
    private String insertObjectIdentity = "insert into acl_object_identity (object_id_class, object_id_identity, owner_sid, entries_inheriting) values (?, ?, ?, ?)";
    private String insertSid = "insert into acl_sid (principal, sid) values (?, ?)";
    private String selectClassPrimaryKey = "select id from acl_class where class=?";
    private String selectObjectIdentityPrimaryKey = "select acl_object_identity.id from acl_object_identity, acl_class where acl_object_identity.object_id_class = acl_class.id and acl_class.class=? and acl_object_identity.object_id_identity = ?";
    private String selectSidPrimaryKey = "select id from acl_sid where principal=? and sid=?";
    private String updateObjectIdentity = "update acl_object_identity set parent_object = ?, owner_sid = ?, entries_inheriting = ? where id = ?";


    public MyJDBCMutableACLService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
        super(dataSource, lookupStrategy);
        Assert.notNull(aclCache, "AclCache required");
        this.aclCache = aclCache;
    }

    public MutableAcl createAcl(ObjectIdentity objectIdentity) throws AlreadyExistsException {
        Assert.notNull(objectIdentity, "Object Identity required");
        if (this.retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
            throw new AlreadyExistsException("Object identity '" + objectIdentity + "' already exists");
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            PrincipalSid sid = new PrincipalSid(auth);
            this.createObjectIdentity(objectIdentity, sid);
            Acl acl = this.readAclById(objectIdentity);
            Assert.isInstanceOf(MutableAcl.class, acl, "MutableAcl should be been returned");
            return (MutableAcl) acl;
        }
    }

    protected void createEntries(final MutableAcl acl) {
        if (!acl.getEntries().isEmpty()) {
            this.jdbcTemplate.batchUpdate(this.insertEntry, new BatchPreparedStatementSetter() {
                public int getBatchSize() {
                    return acl.getEntries().size();
                }

                public void setValues(PreparedStatement stmt, int i) throws SQLException {
                    AccessControlEntry entry_ = (AccessControlEntry) acl.getEntries().get(i);
                    Assert.isTrue(entry_ instanceof AccessControlEntryImpl, "Unknown ACE class");
                    AccessControlEntryImpl entry = (AccessControlEntryImpl) entry_;
                    stmt.setLong(1, (Long) acl.getId());
                    stmt.setInt(2, i);
                    stmt.setLong(3, MyJDBCMutableACLService.this.createOrRetrieveSidPrimaryKey(entry.getSid(), true));
                    stmt.setInt(4, entry.getPermission().getMask());
                    stmt.setBoolean(5, entry.isGranting());
//                    stmt.setBoolean(6, entry.isAuditSuccess());
//                    stmt.setBoolean(7, entry.isAuditFailure());
                    stmt.setBoolean(6, true);
                    stmt.setBoolean(7, true);
                }
            });
        }
    }

    protected void createObjectIdentity(ObjectIdentity object, Sid owner) {
        Long sidId = this.createOrRetrieveSidPrimaryKey(owner, true);
        Long classId = this.createOrRetrieveClassPrimaryKey(object.getType(), true, object.getIdentifier().getClass());
        this.jdbcTemplate.update(this.insertObjectIdentity, new Object[]{classId, object.getIdentifier(), sidId, Boolean.TRUE});
    }

    protected Long createOrRetrieveClassPrimaryKey(String type, boolean allowCreate, Class idType) {
        List<Long> classIds = this.jdbcTemplate.queryForList(this.selectClassPrimaryKey, new Object[]{type}, Long.class);
        if (!classIds.isEmpty()) {
            return (Long) classIds.get(0);
        } else if (allowCreate) {
            if (!this.isAclClassIdSupported()) {
                this.jdbcTemplate.update(this.insertClass, new Object[]{type});
            } else {
                this.jdbcTemplate.update(this.insertClass, new Object[]{type, idType.getCanonicalName()});
            }

            Assert.isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "Transaction must be running");
            return (Long) this.jdbcTemplate.queryForObject(this.classIdentityQuery, Long.class);
        } else {
            return null;
        }
    }

    protected Long createOrRetrieveSidPrimaryKey(Sid sid, boolean allowCreate) {
        Assert.notNull(sid, "Sid required");
        boolean sidIsPrincipal = true;
        String sidName;
        if (sid instanceof PrincipalSid) {
            sidName = ((PrincipalSid) sid).getPrincipal();
        } else {
            if (!(sid instanceof GrantedAuthoritySid)) {
                throw new IllegalArgumentException("Unsupported implementation of Sid");
            }

            sidName = ((GrantedAuthoritySid) sid).getGrantedAuthority();
            sidIsPrincipal = false;
        }

        return this.createOrRetrieveSidPrimaryKey(sidName, sidIsPrincipal, allowCreate);
    }

    protected Long createOrRetrieveSidPrimaryKey(String sidName, boolean sidIsPrincipal, boolean allowCreate) {
        List<Long> sidIds = this.jdbcTemplate.queryForList(this.selectSidPrimaryKey, new Object[]{sidIsPrincipal ? 1 : 0, sidName}, Long.class);
        if (!sidIds.isEmpty()) {
            return (Long) sidIds.get(0);
        } else if (allowCreate) {
            this.jdbcTemplate.update(this.insertSid, new Object[]{sidIsPrincipal ? 1 : 0, sidName});
            Assert.isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "Transaction must be running");
            return (Long) this.jdbcTemplate.queryForObject(this.sidIdentityQuery, Long.class);
        } else {
            return null;
        }
    }

    public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren) throws ChildrenExistException {
        Assert.notNull(objectIdentity, "Object Identity required");
        Assert.notNull(objectIdentity.getIdentifier(), "Object Identity doesn't provide an identifier");
        List children;
        if (deleteChildren) {
            children = this.findChildren(objectIdentity);
            if (children != null) {
                Iterator var4 = children.iterator();

                while (var4.hasNext()) {
                    ObjectIdentity child = (ObjectIdentity) var4.next();
                    this.deleteAcl(child, true);
                }
            }
        } else if (!this.foreignKeysInDatabase) {
            children = this.findChildren(objectIdentity);
            if (children != null) {
                throw new ChildrenExistException("Cannot delete '" + objectIdentity + "' (has " + children.size() + " children)");
            }
        }

        Long oidPrimaryKey = this.retrieveObjectIdentityPrimaryKey(objectIdentity);
        this.deleteEntries(oidPrimaryKey);
        this.deleteObjectIdentity(oidPrimaryKey);
        this.aclCache.evictFromCache(objectIdentity);
    }

    protected void deleteEntries(Long oidPrimaryKey) {
        this.jdbcTemplate.update(this.deleteEntryByObjectIdentityForeignKey, new Object[]{oidPrimaryKey});
    }

    protected void deleteObjectIdentity(Long oidPrimaryKey) {
        this.jdbcTemplate.update(this.deleteObjectIdentityByPrimaryKey, new Object[]{oidPrimaryKey});
    }

    protected Long retrieveObjectIdentityPrimaryKey(ObjectIdentity oid) {
        try {
            return (Long) this.jdbcTemplate.queryForObject(this.selectObjectIdentityPrimaryKey, Long.class, new Object[]{oid.getType(), oid.getIdentifier()});
        } catch (DataAccessException var3) {
            return null;
        }
    }

    public MutableAcl updateAcl(MutableAcl acl) throws NotFoundException {

       /* List<AccessControlEntry> entries = acl.getEntries();
        for (AccessControlEntry e : entries) {
            this.createOrRetrieveSidPrimaryKey(e.getSid(), true);
        }
*/
        Assert.notNull(acl.getId(), "Object Identity doesn't provide an identifier");
        this.deleteEntries(this.retrieveObjectIdentityPrimaryKey(acl.getObjectIdentity()));
        this.createEntries(acl);
        this.updateObjectIdentity(acl);
        this.clearCacheIncludingChildren(acl.getObjectIdentity());
        return (MutableAcl) super.readAclById(acl.getObjectIdentity());
    }

    private void clearCacheIncludingChildren(ObjectIdentity objectIdentity) {
        Assert.notNull(objectIdentity, "ObjectIdentity required");
        List<ObjectIdentity> children = this.findChildren(objectIdentity);
        if (children != null) {
            Iterator var3 = children.iterator();

            while (var3.hasNext()) {
                ObjectIdentity child = (ObjectIdentity) var3.next();
                this.clearCacheIncludingChildren(child);
            }
        }

        this.aclCache.evictFromCache(objectIdentity);
    }

    protected void updateObjectIdentity(MutableAcl acl) {
        Long parentId = null;
        if (acl.getParentAcl() != null) {
            Assert.isInstanceOf(ObjectIdentityImpl.class, acl.getParentAcl().getObjectIdentity(), "Implementation only supports ObjectIdentityImpl");
            ObjectIdentityImpl oii = (ObjectIdentityImpl) acl.getParentAcl().getObjectIdentity();
            parentId = this.retrieveObjectIdentityPrimaryKey(oii);
        }

        Assert.notNull(acl.getOwner(), "Owner is required in this implementation");
        Long ownerSid = this.createOrRetrieveSidPrimaryKey(acl.getOwner(), true);
        int count = this.jdbcTemplate.update(this.updateObjectIdentity, new Object[]{parentId, ownerSid, acl.isEntriesInheriting(), acl.getId()});
        if (count != 1) {
            throw new NotFoundException("Unable to locate ACL to update");
        }
    }

    public void setClassIdentityQuery(String classIdentityQuery) {
        Assert.hasText(classIdentityQuery, "New classIdentityQuery query is required");
        this.classIdentityQuery = classIdentityQuery;
    }

    public void setSidIdentityQuery(String sidIdentityQuery) {
        Assert.hasText(sidIdentityQuery, "New sidIdentityQuery query is required");
        this.sidIdentityQuery = sidIdentityQuery;
    }

    public void setDeleteEntryByObjectIdentityForeignKeySql(String deleteEntryByObjectIdentityForeignKey) {
        this.deleteEntryByObjectIdentityForeignKey = deleteEntryByObjectIdentityForeignKey;
    }

    public void setDeleteObjectIdentityByPrimaryKeySql(String deleteObjectIdentityByPrimaryKey) {
        this.deleteObjectIdentityByPrimaryKey = deleteObjectIdentityByPrimaryKey;
    }

    public void setInsertClassSql(String insertClass) {
        this.insertClass = insertClass;
    }

    public void setInsertEntrySql(String insertEntry) {
        this.insertEntry = insertEntry;
    }

    public void setInsertObjectIdentitySql(String insertObjectIdentity) {
        this.insertObjectIdentity = insertObjectIdentity;
    }

    public void setInsertSidSql(String insertSid) {
        this.insertSid = insertSid;
    }

    public void setClassPrimaryKeyQuery(String selectClassPrimaryKey) {
        this.selectClassPrimaryKey = selectClassPrimaryKey;
    }

    public void setObjectIdentityPrimaryKeyQuery(String selectObjectIdentityPrimaryKey) {
        this.selectObjectIdentityPrimaryKey = selectObjectIdentityPrimaryKey;
    }

    public void setSidPrimaryKeyQuery(String selectSidPrimaryKey) {
        this.selectSidPrimaryKey = selectSidPrimaryKey;
    }

    public void setUpdateObjectIdentity(String updateObjectIdentity) {
        this.updateObjectIdentity = updateObjectIdentity;
    }

    public void setForeignKeysInDatabase(boolean foreignKeysInDatabase) {
        this.foreignKeysInDatabase = foreignKeysInDatabase;
    }

    public void setAclClassIdSupported(boolean aclClassIdSupported) {
        super.setAclClassIdSupported(aclClassIdSupported);
        if (aclClassIdSupported) {
            if (this.insertClass.equals("insert into acl_class (class) values (?)")) {
                this.insertClass = "insert into acl_class (class, class_id_type) values (?, ?)";
            } else {
                log.debug("Insert class statement has already been overridden, so not overridding the default");
            }
        }
    }

    public List<MutableAcl> getAllAcls(Sid sid) {
        List<MutableAcl> listAcls = null;

        Long id = createOrRetrieveSidPrimaryKey(sid, false);
        // select * from acl_entry where sid = id

        return listAcls;
    }

}

