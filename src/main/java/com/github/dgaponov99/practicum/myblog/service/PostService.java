package com.github.dgaponov99.practicum.myblog.service;

import com.github.dgaponov99.practicum.myblog.dto.PostDTO;
import com.github.dgaponov99.practicum.myblog.dto.PostPageDTO;
import com.github.dgaponov99.practicum.myblog.dto.data.PostDataDTO;
import com.github.dgaponov99.practicum.myblog.exception.ImageNotFoundException;
import com.github.dgaponov99.practicum.myblog.exception.PostNotFoundException;
import com.github.dgaponov99.practicum.myblog.mapper.PostMapper;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;
import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import com.github.dgaponov99.practicum.myblog.persistence.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;
    private final PostImageService postImageService;

    public PostPageDTO searchPosts(String title, Set<String> tags, int pageNumber, int pageSize) {
        var postDTOs = postRepository.findAll(title, tags, pageSize, (pageNumber - 1) * pageSize).stream()
                .map(this::toDto)
                .toList();
        var postCount = postRepository.count(title, tags);
        var postPage = new PostPageDTO();
        postPage.setPosts(postDTOs);
        var totalPages = (postCount + pageSize - 1) / pageSize;
        postPage.setLastPage(totalPages);
        postPage.setHasNext(pageNumber < totalPages);
        postPage.setHasPrev(pageNumber > 1);
        return postPage;
    }

    public Optional<PostDTO> getPost(long postId) {
        return getNotDeletedPost(postId).map(this::toDto);
    }

    public PostDTO createPost(PostDataDTO postDataDTO) {
        var post = postRepository.create(postDataDTO.getTitle(), postDataDTO.getText(), postDataDTO.getTags());
        return toDto(post);
    }

    public PostDTO editPost(long id, PostDataDTO postDataDTO) throws PostNotFoundException {
        getNotDeletedPost(id).orElseThrow(() -> new PostNotFoundException(id));
        postRepository.update(id, postDataDTO.getTitle(), postDataDTO.getText(), postDataDTO.getTags());
        return getNotDeletedPost(id).map(this::toDto).orElseThrow();
    }

    public int incrementLikes(long id) throws PostNotFoundException {
        getNotDeletedPost(id).orElseThrow(() -> new PostNotFoundException(id));
        postRepository.incrementLikes(id);
        return getNotDeletedPost(id).map(Post::getLikesCount).orElseThrow();
    }

    public void uploadImage(long id, MultipartFile file) throws PostNotFoundException {
        var post = getNotDeletedPost(id).orElseThrow(() -> new PostNotFoundException(id));
        if (post.getImageUuid() != null) {
            postImageService.deleteImage(post.getImageUuid());
        }
        try (var imageIs = file.getInputStream()) {
            var imageUuid = postImageService.saveImage(imageIs);
            postRepository.updateImageUuid(id, imageUuid);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Optional<Resource> getImage(long id) throws PostNotFoundException {
        var post = getNotDeletedPost(id).orElseThrow(() -> new PostNotFoundException(id));
        if (post.getImageUuid() == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new InputStreamResource(postImageService.getImage(post.getImageUuid())));
        } catch (ImageNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deletePost(long id) {
        postRepository.deleteById(id);
    }

    private Optional<Post> getNotDeletedPost(long id) {
        return postRepository.findById(id).filter(post -> !post.isDeleted());
    }

    private PostDTO toDto(Post post) {
        var postDto = postMapper.toDTO(post);
        postDto.setCommentsCount(commentRepository.countByPostId(post.getId()));
        return postDto;
    }
}
