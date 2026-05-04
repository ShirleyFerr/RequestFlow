package org.sfl.models;

import jakarta.persistence.*;
import org.sfl.enums.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "StatusHistory")
public class StatusHistory {
    @Id
    private Long idStatusHistory;

    @OneToMany
    private Request request;

    @Enumerated(EnumType.STRING)
    private Status oldStatus;

    @Enumerated(EnumType.STRING)
    private Status newStatus;

    private LocalDateTime changedAt;

    private User changedBy;
}
