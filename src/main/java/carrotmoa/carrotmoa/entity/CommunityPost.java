package carrotmoa.carrotmoa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "community_post")
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPost extends BaseEntity {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "community_category_id")
    private Long communityCategoryId;


    public void updateCategory(Long communityCategoryId) {

    }
}