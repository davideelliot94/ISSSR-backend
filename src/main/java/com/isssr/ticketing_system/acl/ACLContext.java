package com.isssr.ticketing_system.acl;

import com.isssr.ticketing_system.acl.entrymanager.MyJDBCMutableACLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@PropertySource({"classpath:application.properties"})
public class ACLContext {

    @Autowired
    DataSource dataSource;

    @Autowired
    MyConsoleAuditLogger myConsoleAuditLogger;

    @Bean
    @Qualifier(value = "aclCache")
    public EhCacheBasedAclCache aclCache() {
        return new EhCacheBasedAclCache(aclEhCacheFactoryBean().getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public EhCacheFactoryBean aclEhCacheFactoryBean() {
        EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
        ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject());
        ehCacheFactoryBean.setCacheName("aclCache");
        ehCacheFactoryBean.setDisabled(true);
        return ehCacheFactoryBean;
    }

    @Bean
    public EhCacheManagerFactoryBean aclCacheManager() {

        return new EhCacheManagerFactoryBean();
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        System.out.println("trying to ask for access");
        return new DefaultPermissionGrantingStrategy(myConsoleAuditLogger);
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority(AuthorityName.ROLE_ADMIN.name()));
    }

    @Bean
    public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        AclPermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService());
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        expressionHandler.setPermissionCacheOptimizer(new AclPermissionCacheOptimizer(aclService()));
        return expressionHandler;
    }

    /*
    @Bean
    @Qualifier(value = "lookupStrategy")
    public LookupStrategy lookupStrategy() {
        System.out.println("trying to ask for access 2");
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), myConsoleAuditLogger);
    }
    */
    @Bean
    public LookupStrategy lookupStrategy() {
        BasicLookupStrategy basicLookupStrategy = new BasicLookupStrategy(dataSource, aclCache(),
                aclAuthorizationStrategy(), myConsoleAuditLogger);
        String lookupObjectIdentitiesWhereClause = "(acl_object_identity.object_id_identity::varchar(36) = ? and acl_class.class = ?)";
        basicLookupStrategy.setLookupObjectIdentitiesWhereClause(lookupObjectIdentitiesWhereClause);
        return basicLookupStrategy;
    }

    @Bean
    public MyJDBCMutableACLService aclService() {
        return new MyJDBCMutableACLService(dataSource, lookupStrategy(), aclCache());
    }

}
