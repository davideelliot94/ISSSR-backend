package com.isssr.ticketing_system.entity.auto_generated.query;

import com.fasterxml.jackson.annotation.JsonView;
import com.isssr.ticketing_system.enumeration.TicketPriority;
import com.isssr.ticketing_system.exception.UpdateException;
import com.isssr.ticketing_system.response_entity.JsonViews;
import lombok.*;
import org.quartz.Job;
import org.quartz.JobKey;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public abstract class ScheduledQuery extends Query implements Job {

    @Transient
    public static final String MAP_ME = "ME";

    @JsonView(JsonViews.Basic.class)
    @NonNull
    protected String cron;

    protected JobKey jobKey; //key in scheduler at runtime execution

    public ScheduledQuery(String description, TicketPriority queryPriority, boolean isEnable, String author, String cron) {
        super(description, queryPriority, isEnable, author);
        this.cron = cron;
    }

    public ScheduledQuery(String description, TicketPriority queryPriority, boolean active, boolean deleted, boolean isEnable, String author, String cron) {
        super(description, queryPriority, active, deleted, isEnable, author);
        this.cron = cron;
    }

    public void updateMe(Query updatedData) throws UpdateException {

        if (!(updatedData instanceof ScheduledQuery))
            throw new UpdateException("Query class doesn't match");

        ScheduledQuery upData = (ScheduledQuery) updatedData;

        super.updateMe(upData);

        this.cron = upData.cron;

    }
}
