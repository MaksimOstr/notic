package com.notic.repository;

import com.notic.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByAuthor_Id(long authorId, Pageable pageable);

    Optional<Note> findByIdAndAuthor_Id(long id, long authorId);

    void deleteByIdAndAuthor_Id(long id, long authorId);
}
