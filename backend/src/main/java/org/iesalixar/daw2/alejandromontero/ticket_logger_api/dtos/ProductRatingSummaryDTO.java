package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRatingSummaryDTO {
    private Long productId;
    private Double averageRating;
    private Long totalReviews;
    private Long oneStar;
    private Long twoStars;
    private Long threeStars;
    private Long fourStars;
    private Long fiveStars;
}
