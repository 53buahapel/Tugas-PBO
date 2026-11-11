package com.tugaspbo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tugaspbo.backend.entity.MenuItem;
import com.tugaspbo.backend.enums.MenuCategory;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

	List<MenuItem> findByDeletedFalse();

	Optional<MenuItem> findByIdAndDeletedFalse(String id);

	List<MenuItem> findByAvailableTrueAndDeletedFalse();

	List<MenuItem> findByCategoryAndDeletedFalse(MenuCategory category);
}
