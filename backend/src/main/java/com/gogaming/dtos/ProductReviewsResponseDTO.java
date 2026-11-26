package com.gogaming.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewsResponseDTO {
    private ProductRatingSummaryDTO summary;
    private List<ResenaResponseDTO> reviews;
}
