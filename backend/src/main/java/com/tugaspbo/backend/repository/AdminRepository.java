package com.tugaspbo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tugaspbo.backend.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {
}
