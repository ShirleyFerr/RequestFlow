package org.sfl.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "RequestComment")
public class RequestComment {
    private Long idRequestComment;

    @OneToMany
    private Request request;

    private String message;

    private LocalDateTime createdAt;

    private User author;
}
