package com.isssr.ticketing_system.entity.SoftDelete;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
@Aspect
public class SoftDeleteAspect {

    private EntityManager entityManager;

    @Autowired
    public SoftDeleteAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Around("@annotation(softDelete)")
    public Object softDeletedMethod(ProceedingJoinPoint joinPoint, SoftDelete softDelete) throws Throwable {
        return this.softDeleted(joinPoint, softDelete.value());
    }

    @Around("@within(softDelete)")
    public Object softDeletedClass(ProceedingJoinPoint joinPoint, SoftDelete softDelete) throws Throwable {
        return this.softDeleted(joinPoint, softDelete.value());
    }

    private Object softDeleted(ProceedingJoinPoint joinPoint, SoftDeleteKind softDeletedKind) throws Throwable {
        Session session = (Session) this.entityManager.getDelegate();
        try {
            if (session.isOpen())
                if (softDeletedKind == SoftDeleteKind.ALL)
                    session.disableFilter("deleted_filter");
                else
                    session.enableFilter("deleted_filter").setParameter("value", softDeletedKind == SoftDeleteKind.DELETED);
            return joinPoint.proceed();
        } finally {
            if (session.isOpen() && softDeletedKind != SoftDeleteKind.ALL)
                session.disableFilter("deleted_filter");
        }
    }
}
