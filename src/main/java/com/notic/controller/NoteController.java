package com.notic.controller;

import com.notic.dto.CreateNoteDto;
import com.notic.dto.JwtAuthUserDto;
import com.notic.dto.UpdateNoteDto;
import com.notic.entity.Note;
import com.notic.response.ApiErrorResponse;
import com.notic.service.NoteService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;


    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Note was successfully created",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"id\": 453,\n" +
                                            "\t\"title\": \"11\",\n" +
                                            "\t\"content\": null,\n" +
                                            "\t\"visibility\": \"PUBLIC\",\n" +
                                            "\t\"createdAt\": \"2025-04-01T17:27:22.498840400Z\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If user was deleted during processing",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"User does not exist\",\t\"status\": 409}")
                    )
            )
    })
    @PostMapping("/create")
    public ResponseEntity<Note> createNote(
            @Valid @RequestBody CreateNoteDto createNoteDto,
            @AuthenticationPrincipal JwtAuthUserDto principal
    ) {
        Note createdNote = noteService.createNote(createNoteDto, principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }


    @GetMapping
    public ResponseEntity<Page<Note>> getNotes(
            @AuthenticationPrincipal JwtAuthUserDto principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Note> pageOfNotes = noteService.getPageOfNotes(principal.getId(), pageable);

        return ResponseEntity.ok(pageOfNotes);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend's notes successfully received"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If user (friends) does not exists or you are not a friends",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"Friend was not found\",\t\"status\": 409}")
                    )
            )
    })
    @GetMapping("/author/{id}")
    public ResponseEntity<Page<Note>> getFriendNotes(
            @PathVariable("id") Long friendId,
            @AuthenticationPrincipal JwtAuthUserDto principal
    ) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> friendPageOfNotes = noteService.getFriendPageOfNotes(principal.getId(), friendId, pageable);

        return ResponseEntity.ok(friendPageOfNotes);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note received",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"id\": 453,\n" +
                                            "\t\"title\": \"11\",\n" +
                                            "\t\"content\": null,\n" +
                                            "\t\"visibility\": \"PUBLIC\",\n" +
                                            "\t\"createdAt\": \"2025-04-01T17:27:22.498840400Z\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If you are not an owner of the note or note id is incorrect",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"Note does not exist\",\t\"status\": 409}")
                    )
            )
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<Note> getNoteById(
            @AuthenticationPrincipal JwtAuthUserDto principal,
            @PathVariable @NotNull Long noteId
    ) {
        Note note = noteService.getNoteByIdAndUserId(noteId, principal.getId());

        return ResponseEntity.ok(note);
    }


    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note was successfully changed",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"id\": 453,\n" +
                                            "\t\"title\": \"11\",\n" +
                                            "\t\"content\": null,\n" +
                                            "\t\"visibility\": \"PUBLIC\",\n" +
                                            "\t\"createdAt\": \"2025-04-01T17:27:22.498840400Z\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "If you are not an owner of the note or note id is incorrect",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject("{\t\"code\": \"Conflict\",\t\"message\": \"Note does not exist\",\t\"status\": 409}")
                    )
            )
    })
    @PatchMapping("/{noteId}")
    public ResponseEntity<Note> updateNote(
            @AuthenticationPrincipal JwtAuthUserDto principal,
            @PathVariable long noteId,
            @RequestBody@Valid UpdateNoteDto body
    ) {
        Note updatedNote = noteService.updateNote(body, noteId, principal.getId());
        return ResponseEntity.ok(updatedNote);
    }


    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNoteById(
            @AuthenticationPrincipal JwtAuthUserDto principal,
            @PathVariable @NotNull Long noteId
    ) {
        noteService.deleteNoteById(noteId, principal.getId());

        return ResponseEntity.ok().build();
    }
}
