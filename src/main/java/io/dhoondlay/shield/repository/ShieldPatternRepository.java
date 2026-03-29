package io.dhoondlay.shield.repository;

import io.dhoondlay.shield.entity.ShieldPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShieldPatternRepository extends JpaRepository<ShieldPattern, Long> {
    List<ShieldPattern> findByEnabledTrue();
}
