package com.shopping.main.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.main.domain.user.entity.SiteUser;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

    Optional<SiteUser> findByEmail(String email);

    boolean existsByEmail(String email);


}
