package io.dhoondlay.shield.service;

import io.dhoondlay.shield.repository.DownstreamRepository;
import io.dhoondlay.shield.repository.ShieldPatternRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("DatabaseInitializationService tests")
class DatabaseInitializationServiceTest {

    @Autowired
    private ShieldPatternRepository patternRepository;

    @Autowired
    private DownstreamRepository downstreamRepository;

    @Autowired
    private DatabaseInitializationService initializationService;

    @Test
    @DisplayName("Patterns and downstreams are seeded on startup")
    void seedsOnStartup() {
        patternRepository.deleteAll();
        downstreamRepository.deleteAll();
        initializationService.init();

        assertThat(patternRepository.count()).isGreaterThan(0);
        assertThat(downstreamRepository.count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("init() does not re-seed if data exists")
    void doesNotReseed() {
        long countBefore = patternRepository.count();
        initializationService.init();
        assertThat(patternRepository.count()).isEqualTo(countBefore);
    }
}
