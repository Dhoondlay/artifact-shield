package io.dhoondlay.shield.repository;

import io.dhoondlay.shield.entity.DownstreamConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DownstreamRepository extends JpaRepository<DownstreamConfig, Long> {
    Optional<DownstreamConfig> findByAlias(String alias);
    Optional<DownstreamConfig> findByAliasAndEnabledTrue(String alias);
    Optional<DownstreamConfig> findFirstByEnabledTrue();
    List<DownstreamConfig> findAllByOrderByAliasAsc();
}
