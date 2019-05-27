package com.isssr.ticketing_system.entity.SoftDelete;

public interface SoftDeletable {
    void delete();

    void restore();

    boolean isDeleted();
}


