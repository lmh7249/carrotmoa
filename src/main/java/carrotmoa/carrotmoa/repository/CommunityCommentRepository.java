package carrotmoa.carrotmoa.repository;

import carrotmoa.carrotmoa.entity.CommunityComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long>, CommunityCommentCustomRepository {
    @Query("select count(c) from CommunityComment c where c.communityPostId = :communityPostId and c.isDeleted = false")
    int countByCommunityPostId(@Param("communityPostId") Long communityPostId);

    Optional<CommunityComment> findByIdAndCommunityPostId(Long commentId, Long communityPostId);

    List<CommunityComment> findByParentId(Long parentId);
}
