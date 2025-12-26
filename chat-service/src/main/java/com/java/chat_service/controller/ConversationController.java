package com.java.chat_service.controller;

import com.java.chat_service.dto.ApiResponse;
import com.java.chat_service.dto.request.ConversationRequest;
import com.java.chat_service.dto.response.ConversationResponse;
import com.java.chat_service.service.ConversationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {
    ConversationService conversationService;

    @PostMapping("/create")
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid ConversationRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.create(request))
                .build();
    }

    @GetMapping("/list")
    ApiResponse<List<ConversationResponse>> myConversations(@RequestParam(required = false) String name) {
        return ApiResponse.<List<ConversationResponse>>builder()
                .result(conversationService.myConversations(name))
                .build();
    }

}