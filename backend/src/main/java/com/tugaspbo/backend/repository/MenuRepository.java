package com.tugaspbo.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tugaspbo.backend.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {

	@EntityGraph(attributePaths = "items")
	Optional<Menu> findFirstByOrderByIdAsc();
}
