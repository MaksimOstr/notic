package com.notic.service;

import com.notic.dto.CreateNoteDto;
import com.notic.dto.UpdateNoteDto;
import com.notic.entity.Note;
import com.notic.entity.User;
import com.notic.enums.NoteVisibilityEnum;
import com.notic.exception.EntityDoesNotExistsException;
import com.notic.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;
    private final FriendshipService friendshipService;


    @Transactional(rollbackFor = EntityDoesNotExistsException.class)
    public Note createNote(CreateNoteDto dto, long userId) {

        User creator = userService.getUserById(userId)
                .orElseThrow(() -> new EntityDoesNotExistsException("User not found"));

        Note note = new Note(
                dto.title(),
                dto.content(),
                creator,
                NoteVisibilityEnum.valueOf(dto.visibility())
        );

        return noteRepository.save(note);
    }


    public Page<Note> getPageOfNotes(long userId, Pageable pageable) {
        List<String> visibility = List.of(
                NoteVisibilityEnum.PUBLIC.name(),
                NoteVisibilityEnum.PROTECTED.name(),
                NoteVisibilityEnum.PRIVATE.name()
        );
        return noteRepository.findByAuthor_Id(userId, visibility, pageable);
    }


    public Page<Note> getFriendPageOfNotes(long userId, long friendId, Pageable pageable) {

        boolean isUser = userService.isUserExistsById(friendId);

        if(!isUser) {
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


    @Transactional(rollbackFor = EntityDoesNotExistsException.class)
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
