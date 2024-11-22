package carrotmoa.carrotmoa.service;

import carrotmoa.carrotmoa.entity.CommunityComment;
import carrotmoa.carrotmoa.entity.CommunityPost;
import carrotmoa.carrotmoa.entity.UserProfile;
import carrotmoa.carrotmoa.enums.NotificationType;
import carrotmoa.carrotmoa.model.request.SaveCommunityCommentRequest;
import carrotmoa.carrotmoa.model.request.SaveCommunityReplyRequest;
import carrotmoa.carrotmoa.model.request.SaveNotificationRequest;
import carrotmoa.carrotmoa.model.response.CommunityCommentResponse;
import carrotmoa.carrotmoa.repository.CommunityCommentRepository;
import carrotmoa.carrotmoa.repository.CommunityPostRepository;
import carrotmoa.carrotmoa.repository.PostRepository;
import carrotmoa.carrotmoa.repository.UserProfileRepository;
import carrotmoa.carrotmoa.util.DateTimeUtil;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityCommentService {
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public Long createCommunityComment(Long communityPostId, SaveCommunityCommentRequest request) {
        // 1. 게시글 조회
        CommunityPost post = communityPostRepository.findById(communityPostId).orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다."));
        // 2. 댓글 엔티티 저장
        request.setCommunityPostId(communityPostId);
        CommunityComment commentEntity = communityCommentRepository.save(request.toCommunityCommentEntity());
//         SSE 알림 보내기.
        // 3. 게시글 작성자의 ID 받아오기
        Long receiverId = postRepository.findUserIdById(post.getPostId());

        if (!request.getUserId().equals(receiverId)) {
            String notificationUrl = "/community/posts/" + communityPostId;
            SaveNotificationRequest saveNotificationRequest = new SaveNotificationRequest(NotificationType.COMMENT, receiverId, request.getUserId(),
                request.getContent(), notificationUrl);
            UserProfile senderUser = userProfileRepository.findNicknameByUserId(commentEntity.getUserId());
            notificationService.sendNotification(receiverId, saveNotificationRequest, senderUser.getNickname(), senderUser.getPicUrl());
        }
        return commentEntity.getId();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findActiveCommentsByCommunityPostId(Long communityPostId) {
        List<CommunityCommentResponse> commentResponseList = communityCommentRepository.findCommentsByPostIdOrdered(communityPostId);

        Map<Long, List<CommunityCommentResponse>> repliesMap = new HashMap<>();
        List<CommunityCommentResponse> topLevelComments = new ArrayList<>();

        for (CommunityCommentResponse comment : commentResponseList) {
            comment.setFormattedCreatedAt(DateTimeUtil.formatElapsedTime(comment.getCreatedAt()));
            comment.setFormattedUpdatedAt(DateTimeUtil.formatElapsedTime(comment.getUpdatedAt()));

            if (comment.getParentId() == null) {
                topLevelComments.add(comment);
            } else {
                repliesMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        for (CommunityCommentResponse topComment : topLevelComments) {
            nestReplies(topComment, repliesMap);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("commentCount", commentResponseList.size());
        responseMap.put("commentList", topLevelComments);
        return responseMap;
    }

    private void nestReplies(CommunityCommentResponse parentComment, Map<Long, List<CommunityCommentResponse>> repliesMap) {
        if (parentComment.isDeleted()) {
            parentComment.setContent("댓글이 삭제되었습니다");
        }

        List<CommunityCommentResponse> replies = repliesMap.get(parentComment.getId());
        if (replies != null) {
            for (CommunityCommentResponse reply : replies) {
                parentComment.addReply(reply);
                nestReplies(reply, repliesMap);
            }
        }
    }





    @Transactional
    public Long createCommunityReply(Long communityPostId, Long commentId, SaveCommunityReplyRequest saveCommunityCommentRequest) {
        // 부모 댓글 조회
        CommunityComment parentComment = communityCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        // 새로운 대댓글 생성
        CommunityComment replyComment = parentComment.createReply(communityPostId, saveCommunityCommentRequest.getUserId(),
            saveCommunityCommentRequest.getContent());

        // 같은 부모 댓글에 대한 최대 orderInGroup 조회
        List<CommunityComment> replies = communityCommentRepository.findByParentId(parentComment.getId());
        int maxOrderInGroup = replies.stream()
            .mapToInt(CommunityComment::getOrderInGroup)
            .max()
            .orElse(0);
        replyComment.setOrderInGroup(maxOrderInGroup + 1); // 최대값 + 1
        // 대댓글 저장
        return communityCommentRepository.save(replyComment).getId(); // 저장 후 ID 반환
    }

    @Transactional
    public Long softDeleteCommentById(Long commentId, Long communityPostId) {
        CommunityComment communityComment = communityCommentRepository
            .findByIdAndCommunityPostId(commentId, communityPostId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        communityComment.softDeleteComment(true);
        return communityComment.getId();
    }
}
