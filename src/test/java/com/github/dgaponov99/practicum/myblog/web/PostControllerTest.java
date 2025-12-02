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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@SpringJUnitConfig(classes = {WebTestConfig.class})
public class PostControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        reset(postService, commentService);
    }

    @Test
    void searchPosts_ok() throws Exception {
        when(postService.searchPosts(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PostPageDTO(List.of(
                        new PostDTO(1, "Заголовок 1", "Текст 1", Set.of("tag1"), 1, 0),
                        new PostDTO(2, "Заголовок 2", "Текст 2", Set.of("tag1"), 1, 0),
                        new PostDTO(3, "Заголовок 3", "Текст 3", Set.of("tag1"), 1, 0)
                ), false, true, 3));

        mockMvc.perform(get("/api/posts")
                        .queryParam("search", "Заголовок #tag1")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts.length()").value(3))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.lastPage").value(3));

        verify(postService, times(1)).searchPosts("Заголовок", Set.of("tag1"), 1, 3);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void searchPosts_empty() throws Exception {
        when(postService.searchPosts(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PostPageDTO(Collections.emptyList(), false, false, 0));

        mockMvc.perform(get("/api/posts")
                        .queryParam("search", "Неизвестный заголовок")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts.length()").value(0))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(0));

        verify(postService, times(1)).searchPosts("Неизвестный заголовок", Collections.emptySet(), 1, 3);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getPost_ok() throws Exception {
        when(postService.getPost(anyLong())).thenReturn(Optional.of(new PostDTO(1, "Заголовок", "Текст", Set.of("tag1", "tag2"), 3, 2)));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок"))
                .andExpect(jsonPath("$.text").value("Текст"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1", "tag2")))
                .andExpect(jsonPath("$.likesCount").value(3))
                .andExpect(jsonPath("$.commentsCount").value(2));

        verify(postService, times(1)).getPost(1);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getPost_notFound() throws Exception {
        when(postService.getPost(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/100500"))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getPost(100500);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void createPost_ok() throws Exception {
        var postDataJson = """
                {
                    "title": "Заголовок",
                    "text": "Текст",
                    "tags": ["tag1", "tag2"]
                  }
                """;
        when(postService.createPost(any())).thenReturn(new PostDTO(1, "Заголовок", "Текст", Set.of("tag1", "tag2"), 0, 0));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postDataJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок"))
                .andExpect(jsonPath("$.text").value("Текст"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1", "tag2")))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));

        verify(postService, times(1)).createPost(new PostDataDTO("Заголовок", "Текст", Set.of("tag1", "tag2")));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void createPost_notValid() throws Exception {
        var postDataJson = """
                {
                    "title": "Заголовок",
                    "tags": ["tag1", "tag2"]
                  }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postDataJson))
                .andExpect(status().is4xxClientError());

        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editPost_ok() throws Exception {
        var postDataJson = """
                {
                    "id": 1,
                    "title": "Новый заголовок",
                    "text": "Новый текст",
                    "tags": ["tag3", "tag4"]
                  }
                """;
        when(postService.editPost(anyLong(), any())).thenReturn(new PostDTO(1L, "Новый заголовок", "Новый текст", Set.of("tag3", "tag4"), 0, 0));

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postDataJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Новый заголовок"))
                .andExpect(jsonPath("$.text").value("Новый текст"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag3", "tag4")))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));

        verify(postService, times(1)).editPost(1L, new PostDataDTO("Новый заголовок", "Новый текст", Set.of("tag3", "tag4")));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editPost_notValid() throws Exception {
        var postDataJson = """
                {
                    "id": 1,
                    "title": "Новый заголовок",
                    "tags": ["tag3", "tag4"]
                  }
                """;

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postDataJson))
                .andExpect(status().is4xxClientError());

        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editPost_notFound() throws Exception {
        var postDataJson = """
                {
                    "id": 100500,
                    "title": "Новый заголовок",
                    "text": "Новый текст",
                    "tags": ["tag3", "tag4"]
                  }
                """;
        when(postService.editPost(anyLong(), any())).thenThrow(new PostNotFoundException(100500));

        mockMvc.perform(put("/api/posts/100500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postDataJson))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).editPost(100500L, new PostDataDTO("Новый заголовок", "Новый текст", Set.of("tag3", "tag4")));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void deletePost() throws Exception {
        doNothing().when(postService).deletePost(anyLong());

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk());

        verify(postService, times(1)).deletePost(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void likePost_ok() throws Exception {
        when(postService.incrementLikes(anyLong())).thenReturn(3);
        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("3"));

        verify(postService, times(1)).incrementLikes(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void likePost_notFound() throws Exception {
        when(postService.incrementLikes(anyLong())).thenThrow(new PostNotFoundException(100500));
        mockMvc.perform(post("/api/posts/100500/likes"))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).incrementLikes(100500L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void uploadImage_ok() throws Exception {
        var pngStub = new byte[]{(byte) 137, 80, 78, 71};
        var image = new MockMultipartFile("image", "image.png", MediaType.IMAGE_PNG_VALUE, pngStub);

        doNothing().when(postService).uploadImage(anyLong(), any());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/1/image")
                        .file(image))
                .andExpect(status().isOk());

        verify(postService, times(1)).uploadImage(1, image);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void uploadImage_notFound() throws Exception {
        var pngStub = new byte[]{(byte) 137, 80, 78, 71};
        var image = new MockMultipartFile("image", "image.png", MediaType.IMAGE_PNG_VALUE, pngStub);

        doThrow(new PostNotFoundException(100500L)).when(postService).uploadImage(anyLong(), any());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/1/image")
                        .file(image))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).uploadImage(1, image);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void downloadImage_ok() throws Exception {
        var pngStub = new byte[]{(byte) 137, 80, 78, 71};
        var image = new ByteArrayResource(pngStub);

        when(postService.getImage(anyLong())).thenReturn(Optional.of(image));

        mockMvc.perform(get("/api/posts/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(content().bytes(pngStub));

        verify(postService, times(1)).getImage(1);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void downloadImage_empty() throws Exception {
        when(postService.getImage(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/1/image"))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getImage(1);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void downloadImage_notPostFound() throws Exception {
        when(postService.getImage(anyLong())).thenThrow(new PostNotFoundException(100500L));

        mockMvc.perform(get("/api/posts/100500/image"))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getImage(100500L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getComments_ok() throws Exception {
        when(commentService.getByPostId(anyLong())).thenReturn(List.of(new CommentDTO(1, 1, "Коммент 1"), new CommentDTO(2, 1, "Коммент 2")));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].postId", everyItem(is(1))))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Коммент 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Коммент 2"));

        verify(commentService, times(1)).getByPostId(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getComments_empty() throws Exception {
        when(commentService.getByPostId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"));

        verify(commentService, times(1)).getByPostId(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getComments_notFound() throws Exception {
        when(commentService.getByPostId(anyLong())).thenThrow(new PostNotFoundException(100500));

        mockMvc.perform(get("/api/posts/100500/comments"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getByPostId(100500L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getComment_ok() throws Exception {
        when(commentService.getComment(anyLong())).thenReturn(Optional.of(new CommentDTO(1, 1, "Комментарий")));

        mockMvc.perform(get("/api/posts/1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.text").value("Комментарий"));

        verify(commentService, times(1)).getComment(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getComment_postNotFound() throws Exception {
        when(commentService.getComment(anyLong())).thenReturn(Optional.of(new CommentDTO(1, 1, "Комментарий")));

        mockMvc.perform(get("/api/posts/100500/comments/1"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getComment(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void getComment_commentNotFound() throws Exception {
        when(commentService.getComment(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/1/comments/100500"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getComment(100500L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void createComment_ok() throws Exception {
        var commentDataJson = """
                {
                    "text": "Комментарий",
                    "postId": 1
                }
                """;
        when(commentService.createComment(anyLong(), any())).thenReturn(new CommentDTO(1, 1, "Комментарий"));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.text").value("Комментарий"));

        verify(commentService, times(1)).createComment(1L, new CommentDataDTO("Комментарий"));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void createComment_notValid() throws Exception {
        var commentDataJson = """
                {
                    "postId": 1
                }
                """;

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().is4xxClientError());

        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void createComment_postNotFound() throws Exception {
        var commentDataJson = """
                {
                    "text": "Комментарий",
                    "postId": 1
                }
                """;
        when(commentService.createComment(anyLong(), any())).thenThrow(new PostNotFoundException(100500));

        mockMvc.perform(post("/api/posts/100500/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).createComment(100500L, new CommentDataDTO("Комментарий"));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editComment_ok() throws Exception {
        var commentDataJson = """
                {
                    "id": 1,
                    "text": "Новый комментарий",
                    "postId": 1
                }
                """;
        when(commentService.getComment(anyLong())).thenReturn(Optional.of(new CommentDTO(1, 1, "Старый комментарий")));
        when(commentService.editComment(anyLong(), any())).thenReturn(new CommentDTO(1, 1, "Новый комментарий"));

        mockMvc.perform(put("/api/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.text").value("Новый комментарий"));

        verify(commentService, times(1)).getComment(1L);
        verify(commentService, times(1)).editComment(1L, new CommentDataDTO("Новый комментарий"));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editComment_notValid() throws Exception {
        var commentDataJson = """
                {
                    "id": 1,
                    "postId": 1
                }
                """;

        mockMvc.perform(put("/api/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().is4xxClientError());

        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editComment_notPostComment() throws Exception {
        var commentDataJson = """
                {
                    "id": 1,
                    "text": "Новый комментарий",
                    "postId": 1
                }
                """;
        when(commentService.getComment(anyLong())).thenReturn(Optional.of(new CommentDTO(1, 100500, "Старый комментарий")));

        mockMvc.perform(put("/api/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getComment(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void editComment_notCommentFound() throws Exception {
        var commentDataJson = """
                {
                    "id": 100500,
                    "text": "Новый комментарий",
                    "postId": 1
                }
                """;
        when(commentService.getComment(anyLong())).thenReturn(Optional.empty());
        when(commentService.editComment(anyLong(), any())).thenThrow(new CommentNotFoundException(100500));

        mockMvc.perform(put("/api/posts/1/comments/100500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDataJson))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getComment(100500L);
        verify(commentService, times(1)).editComment(100500L, new CommentDataDTO("Новый комментарий"));
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void deleteComment_ok() throws Exception {
        when(commentService.getComment(anyLong())).thenReturn(Optional.of(new CommentDTO(1, 1, "Комментарий")));
        doNothing().when(commentService).deleteComment(anyLong());

        mockMvc.perform(delete("/api/posts/1/comments/1"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).getComment(1L);
        verify(commentService, times(1)).deleteComment(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void deleteComment_notPostComment() throws Exception {
        when(commentService.getComment(anyLong())).thenReturn(Optional.of(new CommentDTO(1, 100500, "Комментарий")));

        mockMvc.perform(delete("/api/posts/1/comments/1"))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getComment(1L);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    void deleteComment_notFoundOk() throws Exception {
        when(commentService.getComment(anyLong())).thenReturn(Optional.empty());
        doNothing().when(commentService).deleteComment(anyLong());

        mockMvc.perform(delete("/api/posts/1/comments/100500"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).getComment(100500L);
        verify(commentService, times(1)).deleteComment(100500L);
        verifyNoMoreInteractions(postService, commentService);
    }
}
