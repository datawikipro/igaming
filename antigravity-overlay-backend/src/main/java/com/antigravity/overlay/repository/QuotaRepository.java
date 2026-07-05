package com.antigravity.overlay.repository;

import com.antigravity.overlay.model.Account;
import com.antigravity.overlay.model.Quota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuotaRepository extends JpaRepository<Quota, Long> {
    Optional<Quota> findByAccountAndModelId(Account account, String modelId);
}
