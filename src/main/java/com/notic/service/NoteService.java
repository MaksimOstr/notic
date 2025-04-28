package com.notic.service;

import com.notic.dto.request.CreateNoteDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


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
        List<String> visibility = List.of(
                NoteVisibilityEnum.PUBLIC.name(),
                NoteVisibilityEnum.PROTECTED.name(),
                NoteVisibilityEnum.PRIVATE.name()
        );
        return noteRepository.findByAuthor_Id(userId, visibility, pageable);
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

        return noteRepository.findByAuthor_Id(friendId, visibility, pageable);
    }


    @Transactional
    public Note updateNote(UpdateNoteDto dto, long noteId, long userId) {
        Note note = getNoteByIdAndUserId(noteId, userId);

        dto.getTitle().ifPresent(note::setTitle);
        dto.getContent().ifPresent(note::setContent);
        dto.getVisibility()
                .map(String::toUpperCase)
                .map(NoteVisibilityEnum::valueOf)
                .ifPresent(note::setVisibility);

        return note;
    }


    @Transactional
    public void deleteNoteById(long noteId, long userId) {
        noteRepository.deleteByIdAndAuthor_Id(noteId, userId);
    }


    public Note getNoteByIdAndUserId(long noteId, long userId) {
        return noteRepository.findByIdAndAuthor_Id(noteId, userId)
                .orElseThrow(() -> new EntityDoesNotExistsException("Note not found"));
    }
}
