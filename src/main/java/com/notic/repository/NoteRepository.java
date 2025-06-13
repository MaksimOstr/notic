package com.notic.repository;

import com.notic.entity.Note;
import com.notic.enums.NoteVisibilityEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    @Query("SELECT n FROM Note n WHERE n.author.id = :authorId")
    Page<Note> findByAuthor_Id(
            @Param("authorId")
            long authorId,
            Pageable pageable
    );

    @Query("SELECT n FROM Note n WHERE n.author.id = :authorId AND n.visibility IN :visibility")
    Page<Note> findByAuthor_IdAndVisibility(long authorId, Pageable pageable, List<String> visibility);


    @Transactional
    @Modifying
    @Query("DELETE FROM Note n WHERE n.id = :id")
    void deleteById(long id);
}
