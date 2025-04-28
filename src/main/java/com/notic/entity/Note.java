package com.notic.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.notic.enums.NoteVisibilityEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(nullable = false)
    private String title;

    private String content;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    private User author;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NoteVisibilityEnum visibility;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Note(String title, String content, User author, NoteVisibilityEnum visibility) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.visibility = visibility;
    }
}
