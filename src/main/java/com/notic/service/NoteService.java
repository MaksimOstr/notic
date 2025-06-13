package com.notic.service;

import com.notic.dto.request.CreateNoteDto;
import com.notic.dto.request.NotesByFiltersRequest;
import com.notic.dto.request.UpdateNoteDto;
import com.notic.entity.Note;
import com.notic.entity.User;
import com.notic.enums.NoteVisibilityEnum;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.mapper.NoteMapper;
import com.notic.repository.NoteRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import static com.notic.utils.SpecificationUtils.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final NoteMapper noteMapper;
    private final EntityManager entityManager;


    @Transactional
    public Note createNote(CreateNoteDto dto, long userId) {
        try {
            User user = entityManager.getReference(User.class, userId);
            Note note = noteMapper.toNote(dto, user);

            return noteRepository.save(note);
        } catch (DataIntegrityViolationException e) {
            log.error("Note was not created{}", e.getMessage());
            throw new EntityDoesNotExistsException("Note was not created, creator was not found");
        }
    }

    public Page<Note> getPersonalNotes(long userId, Pageable pageable) {
        return noteRepository.findByAuthor_Id(userId, pageable);
    }

    public Page<Note> getPersonalNotesByFilters(long userId, Pageable pageable, NotesByFiltersRequest dto) {
        Specification<Note> spec = belongsToUser(userId)
                .and(iLike("title", dto.title()))
                .and(iLike("content", dto.content()))
                .and(in("visibility", dto.getVisibility()))
                .and(createdOn(dto.createdAt()));

        return noteRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Note> getFriendPageOfNotes(long userId, long friendId, Pageable pageable) {
        boolean isFriendUser = userService.isUserExistsById(friendId);

        if(!isFriendUser) {
            throw new EntityDoesNotExistsException("Friend was not found");
        }

        boolean isFriends = friendshipService.isFriendshipExistsByUserId(friendId, userId);

        if(!isFriends) {
            throw new EntityDoesNotExistsException("Friendship was not found");
        }

        List<String> visibility = List.of(
                NoteVisibilityEnum.PUBLIC.name(),
                NoteVisibilityEnum.PROTECTED.name()
        );

        return noteRepository.findByAuthor_IdAndVisibility(friendId, pageable, visibility);
    }


    @Transactional
    public Note updateNote(UpdateNoteDto dto, long noteId) {
        Note note = getNoteById(noteId);

        Optional.ofNullable(dto.getTitle()).ifPresent(note::setTitle);
        Optional.ofNullable(dto.getContent()).ifPresent(note::setContent);
        Optional.ofNullable(dto.getVisibility()).ifPresent(visibility -> {
            NoteVisibilityEnum visibilityEnum = NoteVisibilityEnum.fromString(visibility);
            note.setVisibility(visibilityEnum);
        });

        return note;
    }

    public void deleteNoteById(long noteId) {
        noteRepository.deleteById(noteId);
    }

    public Note getNoteById(long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Note not found"));
    }
}
