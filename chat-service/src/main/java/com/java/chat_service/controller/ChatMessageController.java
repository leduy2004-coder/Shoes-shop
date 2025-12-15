package com.java.chat_service.controller;

import com.java.chat_service.dto.ApiResponse;
import com.java.chat_service.dto.request.ChatMessageRequest;
import com.java.chat_service.dto.response.ChatMessageResponse;
import com.java.chat_service.service.ChatMessageService;
import com.java.chat_service.utility.GetInfo;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController {
    ChatMessageService chatMessageService;

    @PostMapping("/create")
    public ApiResponse<ChatMessageResponse> create(
            @RequestBody @Valid ChatMessageRequest request) {
        return ApiResponse.<ChatMessageResponse>builder()
                .result(chatMessageService.create(request))
                .build();
    }

    @GetMapping("/chat/detail")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
            @RequestParam("conversationId") String conversationId) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatMessageService.getMessages(conversationId))
                .build();
    }

    @GetMapping("/user")
    public ApiResponse<List<ChatMessageResponse>> getMessagesByUserId() {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatMessageService.getMessagesByUserId(GetInfo.getLoggedInUserName()))
                .build();
    }

}