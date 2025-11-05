//package com.java.shoes_service.controller;
//
//import com.java.product_service.dto.ApiResponse;
//import com.java.product_service.dto.PageResponse;
//import com.java.product_service.dto.request.CommentCreateRequest;
//import com.java.product_service.dto.response.CommentCreateResponse;
//import com.java.product_service.dto.response.CommentGetResponse;
//import com.java.product_service.service.CommentService;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@RequestMapping("/comment")
//public class CommentController {
//    CommentService commentService;
//
//    @PostMapping(value = "/create")
//    public ApiResponse<CommentCreateResponse> createCommentPost(@RequestBody CommentCreateRequest request) {
//
//        return ApiResponse.<CommentCreateResponse>builder()
//                .result(commentService.saveCommentProduct(request))
//                .build();
//    }
//
//    @GetMapping("/get-by-id/{productId}")
//    ApiResponse<PageResponse<CommentGetResponse>> getCommentPost(
//            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
//            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
//            @PathVariable("productId") String postId
//    ){
//        return ApiResponse.<PageResponse<CommentGetResponse>>builder()
//                .result(commentService.getCommentProduct(page, size, postId))
//                .build();
//    }
//
//    @DeleteMapping(value = "/delete/{commentId}")
//    public ApiResponse<Boolean> deletePost(@PathVariable("commentId") String commentId) {
//
//        Boolean response = commentService.deleteComment(commentId);
//
//        return ApiResponse.<Boolean>builder()
//                .result(response)
//                .build();
//    }
//
//}