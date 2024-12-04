package carrotmoa.carrotmoa.service;

import carrotmoa.carrotmoa.entity.CommunityCategory;
import carrotmoa.carrotmoa.model.response.CommunityCategoryResponse;
import carrotmoa.carrotmoa.model.response.CommunityCategoryResponses;
import carrotmoa.carrotmoa.repository.CommunityCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityCategoryService {
    final private CommunityCategoryRepository categoriesRepository;


    @Cacheable(value = "communityCategories", key = "'subCategories'", cacheManager = "communityCacheManager")
    @Transactional(readOnly = true)
    public CommunityCategoryResponses getSubCategories() {
        log.info("getSubCategories : DB에서 값을 조회하고 있음, 캐시 사용 X");
        List<CommunityCategory> categoriesEntity = categoriesRepository.findByParentIdIsNotNull();
        List<CommunityCategoryResponse> responseList = categoriesEntity.stream().map(CommunityCategoryResponse::new)
            .toList();
        return new CommunityCategoryResponses(responseList);
    }

    @Cacheable(value = "communityCategories", key = "'allCategories'", cacheManager = "communityCacheManager")
    @Transactional(readOnly = true)
    public CommunityCategoryResponses getAllCategories() {
        log.info("getAllCategories : DB에서 값을 조회하고 있음, 캐시 사용 X");
        List<CommunityCategory> categoriesEntity = categoriesRepository.findAll();
        List<CommunityCategoryResponse> allCategories = categoriesEntity.stream().map(CommunityCategoryResponse::new)
            .toList();
        return new CommunityCategoryResponses(allCategories);
    }

}
