package com.github.dgaponov99.practicum.myblog.web;

import com.github.dgaponov99.practicum.myblog.dto.CommentDTO;
import com.github.dgaponov99.practicum.myblog.dto.PostDTO;
import com.github.dgaponov99.practicum.myblog.dto.PostPageDTO;
import com.github.dgaponov99.practicum.myblog.dto.data.CommentDataDTO;
import com.github.dgaponov99.practicum.myblog.dto.data.PostDataDTO;
import com.github.dgaponov99.practicum.myblog.exception.CommentNotFoundException;
import com.github.dgaponov99.practicum.myblog.exception.PostNotFoundException;
import com.github.dgaponov99.practicum.myblog.service.CommentService;
import com.github.dgaponov99.practicum.myblog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;


    @GetMapping()
    public ResponseEntity<PostPageDTO> searchPosts(
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        var title = new StringBuilder();
        var tags = new HashSet<String>();
        if (StringUtils.hasText(search)) {
            for (String s : search.split(" ")) {
                if (s.startsWith("#")) {
                    tags.add(s.substring(1));
                } else {
                    title.append(" ").append(s);
                }
            }
        }
        title = new StringBuilder(title.toString().trim());
        return ResponseEntity.ok(postService.searchPosts(title.toString(), tags, pageNumber, pageSize));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPost(@PathVariable("postId") long postId) {
        return ResponseEntity.of(postService.getPost(postId));
    }

    @PostMapping()
    public ResponseEntity<PostDTO> createPost(@RequestBody @Valid PostDataDTO postDataDTO) {
        return ResponseEntity.ok(postService.createPost(postDataDTO));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDTO> editPost(@PathVariable("postId") long postId,
                                            @RequestBody @Valid PostDataDTO postDataDTO) {
        try {
            return ResponseEntity.ok(postService.editPost(postId, postDataDTO));
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Integer> likePost(@PathVariable("postId") long postId) {
        try {
            return ResponseEntity.ok(postService.incrementLikes(postId));
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/{postId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadImage(@PathVariable("postId") long postId,
                                            @RequestParam("image") MultipartFile image) {
        try {
            postService.uploadImage(postId, image);
            return ResponseEntity.ok().build();
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<Resource> downloadImage(@PathVariable("postId") long postId) {
        try {
            return postService.getImage(postId).map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable("postId") long postId) {
        try {
            return ResponseEntity.ok(commentService.getByPostId(postId));
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable("postId") long postId,
                                                 @PathVariable("commentId") long commentId) {
        var comment = commentService.getComment(commentId);
        if (comment.isPresent()) {
            if (comment.get().getPostId() == postId) {
                return ResponseEntity.ok(comment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(@PathVariable("postId") long postId,
                                                    @RequestBody @Valid CommentDataDTO commentData) {
        try {
            return ResponseEntity.ok(commentService.createComment(postId, commentData));
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDTO> editComment(@PathVariable("postId") long postId,
                                                  @PathVariable("commentId") long commentId,
                                                  @RequestBody @Valid CommentDataDTO commentData) {
        var comment = commentService.getComment(commentId).orElse(null);
        if (comment != null && comment.getPostId() != postId) {
            return ResponseEntity.notFound().build();
        }
        try {
            return ResponseEntity.ok(commentService.editComment(commentId, commentData));
        } catch (CommentNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("postId") long postId,
                                              @PathVariable("commentId") long commentId) {
        var comment = commentService.getComment(commentId).orElse(null);
        if (comment != null && comment.getPostId() != postId) {
            return ResponseEntity.notFound().build();
        }
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

}
