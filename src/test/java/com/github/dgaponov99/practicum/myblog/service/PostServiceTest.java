package com.github.dgaponov99.practicum.myblog.service;


import com.github.dgaponov99.practicum.myblog.dto.PostDTO;
import com.github.dgaponov99.practicum.myblog.dto.data.PostDataDTO;
import com.github.dgaponov99.practicum.myblog.exception.PostNotFoundException;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostServiceTest extends ServiceBaseTest {

    @Autowired
    private PostService postService;

    @Test
    void searchPosts_firstPage() {
        var title = "";
        Set<String> tags = Collections.emptySet();
        var size = 10;
        var page = 1;

        var count = 8;

        var postList = new ArrayList<Post>(size);
        var postDtoList = new ArrayList<PostDTO>(size);
        for (int i = 1; i <= count; i++) {
            postList.add(new Post((long) i, "Заголовок %d".formatted(i), "Текст %d".formatted(i), i, null, Set.of("tag1", "tag2"), false));
            postDtoList.add(new PostDTO(i, "Заголовок %d".formatted(i), "Текст %d".formatted(i), Set.of("tag1", "tag2"), i, 2));
        }


        when(postRepository.findAll(any(), any(), anyInt(), anyInt())).thenReturn(postList);
        when(postRepository.count(any(), any())).thenReturn(count);
        when(commentRepository.countByPostId(any())).thenReturn(2);

        var postPageDto = postService.searchPosts(title, tags, page, size);
        assertNotNull(postPageDto);
        assertEquals(postDtoList, postPageDto.getPosts());
        assertEquals(1, postPageDto.getLastPage());
        assertFalse(postPageDto.isHasNext());
        assertFalse(postPageDto.isHasPrev());

        verify(postRepository, times(1)).findAll(title, tags, size, 0);
        verify(postRepository, times(1)).count(title, tags);
        verify(commentRepository, times(count)).countByPostId(any());
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void searchPosts_middlePage() {
        var title = "";
        Set<String> tags = Collections.emptySet();
        var size = 10;
        var page = 2;

        var count = 100;

        var postList = new ArrayList<Post>(size);
        var postDtoList = new ArrayList<PostDTO>(size);
        for (int i = 1; i <= size; i++) {
            postList.add(new Post((long) i, "Заголовок %d".formatted(i), "Текст %d".formatted(i), i, null, Set.of("tag1", "tag2"), false));
            postDtoList.add(new PostDTO(i, "Заголовок %d".formatted(i), "Текст %d".formatted(i), Set.of("tag1", "tag2"), i, 2));
        }


        when(postRepository.findAll(any(), any(), anyInt(), anyInt())).thenReturn(postList);
        when(postRepository.count(any(), any())).thenReturn(count);
        when(commentRepository.countByPostId(any())).thenReturn(2);

        var postPageDto = postService.searchPosts(title, tags, page, size);
        assertNotNull(postPageDto);
        assertEquals(postDtoList, postPageDto.getPosts());
        assertEquals(10, postPageDto.getLastPage());
        assertTrue(postPageDto.isHasNext());
        assertTrue(postPageDto.isHasPrev());

        verify(postRepository, times(1)).findAll(title, tags, size, 10);
        verify(postRepository, times(1)).count(title, tags);
        verify(commentRepository, times(size)).countByPostId(any());
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void searchPosts_lastPage() {
        var title = "";
        Set<String> tags = Collections.emptySet();
        var size = 10;
        var page = 10;

        var count = 99;

        var postList = new ArrayList<Post>(size);
        var postDtoList = new ArrayList<PostDTO>(size);
        for (int i = 1; i < size; i++) {
            postList.add(new Post((long) i, "Заголовок %d".formatted(i), "Текст %d".formatted(i), i, null, Set.of("tag1", "tag2"), false));
            postDtoList.add(new PostDTO(i, "Заголовок %d".formatted(i), "Текст %d".formatted(i), Set.of("tag1", "tag2"), i, 2));
        }


        when(postRepository.findAll(any(), any(), anyInt(), anyInt())).thenReturn(postList);
        when(postRepository.count(any(), any())).thenReturn(count);
        when(commentRepository.countByPostId(any())).thenReturn(2);

        var postPageDto = postService.searchPosts(title, tags, page, size);
        assertNotNull(postPageDto);
        assertEquals(postDtoList, postPageDto.getPosts());
        assertEquals(10, postPageDto.getLastPage());
        assertFalse(postPageDto.isHasNext());
        assertTrue(postPageDto.isHasPrev());

        verify(postRepository, times(1)).findAll(title, tags, size, 90);
        verify(postRepository, times(1)).count(title, tags);
        verify(commentRepository, times(size - 1)).countByPostId(any());
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void searchPosts_empty() {
        var title = "Несуществующий заголовок";
        Set<String> tags = Set.of("tag100", "tag200");
        var size = 10;
        var page = 1;

        var count = 0;


        when(postRepository.findAll(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(postRepository.count(any(), any())).thenReturn(count);
        when(commentRepository.countByPostId(any())).thenReturn(2);

        var postPageDto = postService.searchPosts(title, tags, page, size);
        assertNotNull(postPageDto);
        assertTrue(postPageDto.getPosts().isEmpty());
        assertEquals(0, postPageDto.getLastPage());
        assertFalse(postPageDto.isHasNext());
        assertFalse(postPageDto.isHasPrev());

        verify(postRepository, times(1)).findAll(title, tags, size, 0);
        verify(postRepository, times(1)).count(title, tags);
        verify(commentRepository, times(0)).countByPostId(any());
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getPost_success() {
        var postId = 1L;
        var title = "Заголовок";
        var text = "Текст";
        var tags = Set.of("tag1", "tag2");
        var expectedPostDTO = new PostDTO(postId, title, text, tags, 10, 3);

        var post = new Post(postId, title, text, 10, null, tags, false);
        when(commentRepository.countByPostId(anyLong())).thenReturn(3);
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        var postDTO = postService.getPost(postId).orElseThrow();

        assertEquals(expectedPostDTO, postDTO);

        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).countByPostId(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getPost_notFound() {
        var postId = 1L;

        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        var postDTOOpt = postService.getPost(postId);
        assertTrue(postDTOOpt.isEmpty());

        verify(postRepository, times(1)).findById(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getPost_deleted() {
        var postId = 1L;

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(postId, "Заголовок", "Текст", 3, null, Collections.emptySet(), true)));

        var postDTOOpt = postService.getPost(postId);
        assertTrue(postDTOOpt.isEmpty());

        verify(postRepository, times(1)).findById(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    public void createPost_success() {
        var postId = 1L;
        var title = "Заголовок";
        var text = "Текст";
        var tags = Set.of("tag1", "tag2");
        var postDataDTO = new PostDataDTO(title, text, tags);

        var expectedPostDTO = new PostDTO(postId, title, text, tags, 0, 0);

        var post = new Post(postId, title, text, 0, null, tags, false);
        when(postRepository.create(any(), any(), any())).thenReturn(post);
        when(commentRepository.countByPostId(anyLong())).thenReturn(0);

        var postDTO = postService.createPost(postDataDTO);

        verify(postRepository, times(1)).create(title, text, tags);
        verify(commentRepository, times(1)).countByPostId(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
        assertEquals(expectedPostDTO, postDTO);
    }

    @Test
    void editPost_success() {
        var postId = 1L;
        var title = "Новый заголовок";
        var text = "Новый текст";
        var tags = Set.of("tag3", "tag4");
        var postDataDTO = new PostDataDTO(title, text, tags);
        var imageUuid = UUID.randomUUID();

        var expectedPostDTO = new PostDTO(postId, title, text, tags, 10, 3);

        when(commentRepository.countByPostId(anyLong())).thenReturn(3);
        when(postRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Post(postId, "Старый заголовок", "Старый текст", 10, imageUuid, Set.of("tag1", "tag2"), false)))
                .thenReturn(Optional.of(new Post(postId, title, text, 10, imageUuid, tags, false)));

        assertDoesNotThrow(() -> {
            var postDto = postService.editPost(postId, postDataDTO);
            assertEquals(expectedPostDTO, postDto);
        });

        verify(postRepository, times(2)).findById(postId);
        verify(commentRepository, times(1)).countByPostId(postId);
        verify(postRepository, times(1)).update(postId, title, text, tags);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void editPost_failNotFound() {
        var postId = 1L;
        var title = "Новый заголовок";
        var text = "Новый текст";
        var tags = Set.of("tag3", "tag4");
        var postDataDTO = new PostDataDTO(title, text, tags);

        when(commentRepository.countByPostId(anyLong())).thenReturn(3);
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.editPost(postId, postDataDTO));

        verify(postRepository, times(1)).findById(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void incrementLikes_success() {
        var postId = 1L;
        var title = "Заголовок";
        var text = "Текст";
        var tags = Set.of("tag1", "tag2");
        var imageUuid = UUID.randomUUID();

        var likes = 10;
        var expectedLikes = 11;

        doNothing().when(postRepository).incrementLikes(anyLong());
        when(postRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Post(postId, title, text, likes, imageUuid, tags, false)))
                .thenReturn(Optional.of(new Post(postId, title, text, expectedLikes, imageUuid, tags, false)));

        assertDoesNotThrow(() -> {
            var newLikes = postService.incrementLikes(postId);
            assertEquals(expectedLikes, newLikes);
        });

        verify(postRepository, times(2)).findById(postId);
        verify(postRepository, times(1)).incrementLikes(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void incrementLikes_deleted() {
        var postId = 1L;
        var likes = 10;

        when(postRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Post(postId, "Заголовок", "Текст", likes, null, Set.of("tag1", "tag2"), true)));

        assertThrows(PostNotFoundException.class, () -> postService.incrementLikes(postId));

        verify(postRepository, times(1)).findById(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void uploadImage_success() {
        var postId = 1L;
        var imageUuid = UUID.randomUUID();
        var imageByes = "some-image".getBytes();
        var multipartFile = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, imageByes);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(1L, "Заголовок", "Текст", 0, null, Set.of("tag1", "tag2"), false)));
        doNothing().when(postImageService).deleteImage(any());
        doNothing().when(postRepository).updateImageUuid(anyLong(), any());
        when(postImageService.saveImage(any())).thenAnswer((invocation) -> {
            assertArrayEquals(imageByes, invocation.getArgument(0, InputStream.class).readAllBytes());
            return imageUuid;
        });

        assertDoesNotThrow(() -> postService.uploadImage(postId, multipartFile));

        verify(postRepository, times(1)).findById(postId);
        verify(postImageService, times(0)).deleteImage(any());
        verify(postImageService, times(1)).saveImage(any());
        verify(postRepository, times(1)).updateImageUuid(postId, imageUuid);
        verifyNoMoreInteractions(postRepository, postImageService, commentRepository);
    }

    @Test
    void uploadImage_successWithRemoveOld() {
        var postId = 1L;
        var oldImageUuid = UUID.randomUUID();
        var imageUuid = UUID.randomUUID();
        var imageBytes = "some-image".getBytes();
        var multipartFile = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, imageBytes);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(1L, "Заголовок", "Текст", 0, oldImageUuid, Set.of("tag1", "tag2"), false)));
        doNothing().when(postImageService).deleteImage(any());
        doNothing().when(postRepository).updateImageUuid(anyLong(), any());
        when(postImageService.saveImage(any())).thenAnswer((invocation) -> {
            assertArrayEquals(imageBytes, invocation.getArgument(0, InputStream.class).readAllBytes());
            return imageUuid;
        });

        assertDoesNotThrow(() -> postService.uploadImage(postId, multipartFile));

        verify(postRepository, times(1)).findById(postId);
        verify(postImageService, times(1)).deleteImage(oldImageUuid);
        verify(postImageService, times(1)).saveImage(any());
        verify(postRepository, times(1)).updateImageUuid(postId, imageUuid);
        verifyNoMoreInteractions(postRepository, postImageService, commentRepository);
    }

    @Test
    void uploadImage_failNotFound() {
        var postId = 1L;
        var imageBytes = "some-image".getBytes();
        var multipartFile = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, imageBytes);

        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.uploadImage(postId, multipartFile));

        verify(postRepository, times(1)).findById(postId);
        verifyNoMoreInteractions(postRepository, postImageService, commentRepository);
    }

    @Test
    void getImage_success() throws Exception {
        var postId = 1L;
        var imageBytes = "some-image".getBytes();
        var imageUuid = UUID.randomUUID();

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(1L, "Заголовок", "Текст", 0, imageUuid, Set.of("tag1", "tag2"), false)));
        when(postImageService.getImage(any())).thenReturn(new ByteArrayInputStream(imageBytes));

        assertDoesNotThrow(() -> {
            var imageResource = postService.getImage(postId).orElseThrow();
            assertArrayEquals(imageBytes, imageResource.getContentAsByteArray());
        });

        verify(postRepository, times(1)).findById(postId);
        verify(postImageService, times(1)).getImage(imageUuid);
        verifyNoMoreInteractions(postRepository, postImageService, commentRepository);
    }

    @Test
    void getImage_empty() throws Exception {
        var postId = 1L;

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(1L, "Заголовок", "Текст", 0, null, Set.of("tag1", "tag2"), false)));

        assertDoesNotThrow(() -> {
            var imageResourceOpt = postService.getImage(postId);
            assertTrue(imageResourceOpt.isEmpty());
        });

        verify(postRepository, times(1)).findById(postId);
        verify(postImageService, times(0)).getImage(any());
        verifyNoMoreInteractions(postRepository, postImageService, commentRepository);
    }

    @Test
    void getImage_postNotFound() throws Exception {
        var postId = 1L;

        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getImage(postId));

        verify(postRepository, times(1)).findById(postId);
        verify(postImageService, times(0)).getImage(any());
        verifyNoMoreInteractions(postRepository, postImageService, commentRepository);
    }

    @Test
    void deletePost() {
        var postId = 100500L;
        doNothing().when(postRepository).deleteById(anyLong());

        postService.deletePost(postId);

        verify(postRepository, times(1)).deleteById(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }
}
