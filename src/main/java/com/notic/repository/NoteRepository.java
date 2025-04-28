package com.notic.repository;

import com.notic.entity.Note;
import com.notic.enums.NoteVisibilityEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("SELECT n FROM Note n WHERE n.author.id = :authorId AND n.visibility IN :visibility")
    Page<Note> findByAuthor_Id(
            @Param("authorId")
            long authorId,
            @Param("visibility")
            Collection<String> visibility,
            Pageable pageable
    );

    Optional<Note> findByIdAndAuthor_Id(long id, long authorId);

    void deleteByIdAndAuthor_Id(long id, long authorId);
}
