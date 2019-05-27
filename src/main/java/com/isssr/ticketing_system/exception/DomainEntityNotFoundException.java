package com.isssr.ticketing_system.exception;


import lombok.Getter;

public class DomainEntityNotFoundException extends RuntimeException {
    @Getter
    private long entityId;
    @Getter
    private Class entityClass;

    public DomainEntityNotFoundException(long entityId, Class entityClass) {
        this.entityClass = entityClass;
        this.entityId = entityId;
    }
}
