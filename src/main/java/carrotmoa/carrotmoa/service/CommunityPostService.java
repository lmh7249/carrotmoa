package carrotmoa.carrotmoa.service;

import carrotmoa.carrotmoa.entity.CommunityPost;
import carrotmoa.carrotmoa.entity.Post;
import carrotmoa.carrotmoa.model.request.SaveCommunityPostRequest;
import carrotmoa.carrotmoa.model.request.SavePostImageRequest;
import carrotmoa.carrotmoa.model.request.UpdateCommunityPostRequest;
import carrotmoa.carrotmoa.model.response.CommunityPostDetailResponse;
import carrotmoa.carrotmoa.model.response.CommunityPostListResponse;
import carrotmoa.carrotmoa.repository.CommunityCommentRepository;
import carrotmoa.carrotmoa.repository.CommunityPostRepository;
import carrotmoa.carrotmoa.repository.PostImageRepository;
import carrotmoa.carrotmoa.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CommunityPostService {
    private static final Logger log = LoggerFactory.getLogger(CommunityPostService.class);
    // TODO: 검증로직 구현 완료, 에러 관련 뷰는 추후 제작 예정.
    final private PostRepository postRepository;
    final private CommunityPostRepository communityPostRepository;
    final private PostImageRepository postImageRepository;
    final private CommunityCommentRepository communityCommentRepository;

    @Transactional
    public Long createCommunityPost(SaveCommunityPostRequest communityPostRequestDto) {
        validateInput(communityPostRequestDto);
        // TODO: 커뮤니티 게시판의 카테고리 고유번호는 5L -> 이걸 여기서 default 값으로 지정하는 것보다 다른 방법 찾아보기.
        communityPostRequestDto.setServiceId(4L);
        Post saveEntity = postRepository.save(communityPostRequestDto.toPostEntity());
        CommunityPost save = communityPostRepository.save(communityPostRequestDto.toCommunityPostEntity(saveEntity.getId()));

        List<String> imageUrls = extractImageUrls(communityPostRequestDto.getContent());
        log.info("url 저장되는지 확인 로그 {}", imageUrls);
        if (!imageUrls.isEmpty()) {
            savePostImages(saveEntity.getId(), imageUrls);
        }
        return new SaveCommunityPostRequest(save).getCommunityPostId();
    }

    @Transactional(readOnly = true)
    public CommunityPostDetailResponse findCommunityPostDetail(Long id) {
        return communityPostRepository.findCommunityPostDetail(id);
    }

    @Transactional(readOnly = true)
    public List<CommunityPostListResponse> getAllCommunityPosts() {
        Long serviceId = 4L;
        List<CommunityPostListResponse> posts = postRepository.getAllCommunityPosts(serviceId);
        for (CommunityPostListResponse post : posts) {
            int commentCount = communityCommentRepository.countByCommunityPostId(post.getCommunityPostId());
            post.setCommentCount(commentCount);
        }
        return posts;
    }

    @Transactional
    public int deleteByCommunityPostId(Long communityPostId) {
        int deletedCount = postRepository.deleteByCommunityPostId(communityPostId);
        if (deletedCount == 0) {
            // TODO: 사용자 정의 예외를 만들어보자.
            // TODO: 게시글이 삭제되면 해당 게시글의 댓글도 전부 삭제하자. 아래에 communityPostId 받아와서 그에 해당하는 댓글 삭제
            throw new RuntimeException("앗! 삭제할 게시글이 존재하지 않아요.");
        }
        return deletedCount;
    }

    @Transactional
    public int updateCommunityPost(Long communityPostId, UpdateCommunityPostRequest request) {
        CommunityPost communityPost = communityPostRepository.findById(communityPostId).orElseThrow(RuntimeException::new);
        Post post = postRepository.findById(communityPost.getPostId()).orElseThrow(RuntimeException::new);
        // 방법 1
        /* // request에서 가져온 값으로 수정하기 (entity에 setter 사용)
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            communityPost.setCommunityCategoryId(request.getCommunityCategoryId());

        * 방법2
        * // 엔티티에 수정 메서드를 생성 -> 호출해서 값 수정(setter처럼 실수할 일이 없음)
            post.updatePost(request.getTitle(), request.getContent());
            communityPost.updateCategory(request.getCommunityCategoryId());
        * */

        post.updatePost(request.getTitle(), request.getContent());
        communityPost.updateCategory(request.getCommunityCategoryId());

        return 0;
    }





    private List<String> extractImageUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img[^>]+src=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            imageUrls.add(matcher.group(1));
        }
        return imageUrls;
    }

    private void savePostImages(Long postIdList, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            SavePostImageRequest savePostImageRequest = new SavePostImageRequest(postIdList, imageUrl);
            postImageRepository.save(savePostImageRequest.toPostImageEntity());
        }
    }

    private void validateInput(SaveCommunityPostRequest communityPostRequestDto) {
        validateBlank(communityPostRequestDto.getTitle(), "Title");
        validateBlank(communityPostRequestDto.getContent(), "Content");
    }

    private void validateBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "필드는 입력 필수입니다.");
        }
    }
}