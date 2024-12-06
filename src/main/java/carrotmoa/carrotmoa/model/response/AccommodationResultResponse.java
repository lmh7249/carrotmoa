package carrotmoa.carrotmoa.model.response;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccommodationResultResponse {
    private long id;
    private String title;
    private String roadAddress;
    private BigDecimal price;
    private String imageUrl;
    private List<Integer> roomTypeCount;

    public static AccommodationResultResponse fromData(Object[] data) {
        List<Integer> roomTypeCounts = Arrays.asList(
            (Integer) data[5],
            (Integer) data[6],
            (Integer) data[7],
            (Integer) data[8]
        );

        return AccommodationResultResponse.builder()
            .id((Long) data[0])
            .title((String) data[1])
            .roadAddress((String) data[2])
            .price((BigDecimal) data[3])
            .imageUrl((String) data[4])
            .roomTypeCount(roomTypeCounts)
            .build();
    }
}