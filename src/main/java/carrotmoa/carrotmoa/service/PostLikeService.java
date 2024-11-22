package carrotmoa.carrotmoa.service;

import carrotmoa.carrotmoa.entity.PostLike;
import carrotmoa.carrotmoa.repository.CommunityPostRepository;
import carrotmoa.carrotmoa.repository.PostLikeRepository;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private static final String LIKE_KEY_FORMAT = "post:%d:likes";
    private static final String LIKE_COUNT_KEY_FORMAT = "post:%d:likeCount";
    private final PostLikeRepository postLikeRepository;
    private final CommunityPostRepository communityPostRepository;
    private final RedisTemplate<String, Object> likeRedisTemplate;

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Long communityPostId = communityPostRepository.findPostIdById(postId);
        String redisKey = String.format(LIKE_KEY_FORMAT, communityPostId);

        // Redis의 Set에서 유저 ID 존재 여부로 좋아요 상태를 확인
        Boolean isLiked = likeRedisTemplate.opsForSet().isMember(redisKey, userId);

        if (isLiked != null && isLiked) {
            // 좋아요가 이미 존재하면 취소 (Set에서 제거)
            likeRedisTemplate.opsForSet().remove(redisKey, userId);
            updateDatabaseLikeStatus(communityPostId, userId, true); // 취소된 상태로 DB 업데이트
            updateLikeCount(communityPostId, true); // 좋아요 개수 감소
            return false;
        } else {
            // 좋아요가 없으면 추가 (Set에 추가)
            likeRedisTemplate.opsForSet().add(redisKey, userId);
            updateDatabaseLikeStatus(communityPostId, userId, false); // 활성 상태로 DB 업데이트
            updateLikeCount(communityPostId, false); // 좋아요 개수 증가
            return true;
        }
    }

    private void updateDatabaseLikeStatus(Long postId, Long userId, boolean isCanceled) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isPresent()) {
            existingLike.get().setCanceled(isCanceled);
            postLikeRepository.save(existingLike.get());
        } else {
            postLikeRepository.save(new PostLike(postId, userId, "LIKE", isCanceled));
        }
    }

    // 좋아요 개수 업데이트 메서드 (동일)
    private void updateLikeCount(Long postId, boolean isCanceled) {
        String likeCountKey = String.format(LIKE_COUNT_KEY_FORMAT, postId);

        if (!isCanceled) {
            likeRedisTemplate.opsForValue().increment(likeCountKey);
        } else {
            likeRedisTemplate.opsForValue().decrement(likeCountKey);
        }
        likeRedisTemplate.expire(likeCountKey, 1, TimeUnit.DAYS);
    }

    public int getLikeCount(Long postId) {
        Long communityPostId = communityPostRepository.findPostIdById(postId);
        String likeCountKey = String.format(LIKE_COUNT_KEY_FORMAT, communityPostId);
        Integer likeCount = (Integer) likeRedisTemplate.opsForValue().get(likeCountKey);

        if (likeCount == null) {
            likeCount = postLikeRepository.countByPostIdAndIsCanceledFalse(communityPostId);
            likeRedisTemplate.opsForValue().set(likeCountKey, likeCount, 1, TimeUnit.DAYS);
        }
        return likeCount;
    }

    public Boolean findIsCanceledByPostIdAndUserId(Long postId, Long userId) {
        Long communityPostId = communityPostRepository.findPostIdById(postId);
        return postLikeRepository.findIsCanceledByPostIdAndUserId(communityPostId, userId).orElse(true);
    }
}