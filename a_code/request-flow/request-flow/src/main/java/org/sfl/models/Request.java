package org.sfl.models;

import jakarta.persistence.*;
import org.sfl.enums.Category;
import org.sfl.enums.Priority;
import org.sfl.enums.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "Request")
public class Request {
    private Long idRequest;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    @OneToMany
    private User requester;

    @OneToMany
    private User assignedTo;
}
