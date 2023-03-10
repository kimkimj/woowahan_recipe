package com.woowahan.recipe.domain.dto.recipeDto;

import com.woowahan.recipe.domain.entity.RecipeItemEntity;
import com.woowahan.recipe.domain.entity.ReviewEntity;
import lombok.*;

import javax.swing.text.html.parser.Entity;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecipeFindResDto {
    private Long recipeId;
    private String recipeTitle;
    private String recipeBody;
    private String userName;
    private int recipeLike;
    private int recipeView;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private List<RecipeItemEntity> items;
    private List<ReviewEntity> reviews;
    private String recipeImagePath;
}
