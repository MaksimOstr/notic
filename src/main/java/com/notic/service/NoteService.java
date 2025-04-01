package com.notic.service;

import com.notic.dto.CreateNoteDto;
import com.notic.dto.UpdateNoteDto;
import com.notic.entity.Note;
import com.notic.entity.User;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;


    @Transactional(rollbackFor = EntityDoesNotExistsException.class)
    public Note createNote(CreateNoteDto dto, long userId) {

        User creator = userService.getUserById(userId)
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));

        Note note = new Note(
                dto.title(),
                dto.content(),
                creator,
                dto.visibility()
        );

        return noteRepository.save(note);
    }

    public Page<Note> getPageOfNotes(long userId, Pageable pageable) {
        return noteRepository.findByAuthor_Id(userId, pageable);
    }


    @Transactional(rollbackFor = EntityDoesNotExistsException.class)
    public Note updateNote(UpdateNoteDto dto, long noteId, long userId) {
        Note note = getNoteByIdAndUserId(noteId, userId);

        dto.getTitle().ifPresent(note::setTitle);
        dto.getContent().ifPresent(note::setContent);
        dto.getVisibility().ifPresent(note::setVisibility);

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
