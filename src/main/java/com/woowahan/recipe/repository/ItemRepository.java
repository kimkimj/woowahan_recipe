package com.woowahan.recipe.repository;

import com.woowahan.recipe.domain.entity.ItemEntity;
import com.woowahan.recipe.domain.entity.RecipeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    Optional<ItemEntity> findByName(String name);
    Page<ItemEntity> findByNameContaining(String keyword, Pageable pageable);

}
