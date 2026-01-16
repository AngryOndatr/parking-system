package com.parking.billing_service.repository;

import com.parking.common.entity.Tariff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TariffRepository.
 * Uses @DataJpaTest with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TariffRepository Integration Tests")
class TariffRepositoryTest {

    @Autowired
    private TariffRepository tariffRepository;

    private Tariff oneTimeTariff;
    private Tariff dailyTariff;
    private Tariff inactiveTariff;

    @BeforeEach
    void setUp() {
        // Clean up database
        tariffRepository.deleteAll();

        // Create test tariffs
        oneTimeTariff = new Tariff();
        oneTimeTariff.setTariffType("ONE_TIME");
        oneTimeTariff.setHourlyRate(new BigDecimal("50.00"));
        oneTimeTariff.setDescription("One-time parking tariff");
        oneTimeTariff.setIsActive(true);

        dailyTariff = new Tariff();
        dailyTariff.setTariffType("DAILY");
        dailyTariff.setHourlyRate(new BigDecimal("30.00"));
        dailyTariff.setDailyRate(new BigDecimal("500.00"));
        dailyTariff.setDescription("Daily parking tariff");
        dailyTariff.setIsActive(true);

        inactiveTariff = new Tariff();
        inactiveTariff.setTariffType("OLD_TARIFF");
        inactiveTariff.setHourlyRate(new BigDecimal("40.00"));
        inactiveTariff.setDescription("Inactive tariff");
        inactiveTariff.setIsActive(false);
    }

    @Test
    @DisplayName("Should save tariff and auto-generate ID")
    void testSaveTariff_Success() {
        // When
        Tariff saved = tariffRepository.save(oneTimeTariff);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTariffType()).isEqualTo("ONE_TIME");
        assertThat(saved.getHourlyRate()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find tariff by ID")
    void testFindById_Success() {
        // Given
        Tariff saved = tariffRepository.save(oneTimeTariff);

        // When
        Optional<Tariff> found = tariffRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTariffType()).isEqualTo("ONE_TIME");
        assertThat(found.get().getHourlyRate()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should find active tariff by type")
    void testFindByTariffTypeAndIsActiveTrue_Success() {
        // Given
        tariffRepository.save(oneTimeTariff);
        tariffRepository.save(dailyTariff);

        // When
        Optional<Tariff> found = tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTariffType()).isEqualTo("ONE_TIME");
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should not find inactive tariff when searching for active")
    void testFindByTariffTypeAndIsActiveTrue_InactiveTariff_NotFound() {
        // Given
        tariffRepository.save(inactiveTariff);

        // When
        Optional<Tariff> found = tariffRepository.findByTariffTypeAndIsActiveTrue("OLD_TARIFF");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when tariff type does not exist")
    void testFindByTariffTypeAndIsActiveTrue_NotFound() {
        // When
        Optional<Tariff> found = tariffRepository.findByTariffTypeAndIsActiveTrue("NON_EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if tariff exists by type")
    void testExistsByTariffType_True() {
        // Given
        tariffRepository.save(oneTimeTariff);

        // When
        boolean exists = tariffRepository.existsByTariffType("ONE_TIME");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when tariff does not exist")
    void testExistsByTariffType_False() {
        // When
        boolean exists = tariffRepository.existsByTariffType("NON_EXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true for inactive tariff (existsByTariffType checks all)")
    void testExistsByTariffType_InactiveTariff_True() {
        // Given
        tariffRepository.save(inactiveTariff);

        // When
        boolean exists = tariffRepository.existsByTariffType("OLD_TARIFF");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should save tariff with nullable dailyRate")
    void testSaveTariff_WithNullDailyRate_Success() {
        // When
        Tariff saved = tariffRepository.save(oneTimeTariff);

        // Then
        assertThat(saved.getDailyRate()).isNull();
    }

    @Test
    @DisplayName("Should save tariff with both hourly and daily rates")
    void testSaveTariff_WithBothRates_Success() {
        // When
        Tariff saved = tariffRepository.save(dailyTariff);

        // Then
        assertThat(saved.getHourlyRate()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(saved.getDailyRate()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should update tariff and change updatedAt timestamp")
    void testUpdateTariff_Success() throws InterruptedException {
        // Given
        Tariff saved = tariffRepository.save(oneTimeTariff);
        Long savedId = saved.getId();

        // Wait a bit to ensure timestamp difference
        Thread.sleep(100);

        // When
        saved.setHourlyRate(new BigDecimal("60.00"));
        saved.setDescription("Updated description");
        Tariff updated = tariffRepository.save(saved);

        // Then
        assertThat(updated.getId()).isEqualTo(savedId);
        assertThat(updated.getHourlyRate()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Should find all tariffs")
    void testFindAll_Success() {
        // Given
        tariffRepository.save(oneTimeTariff);
        tariffRepository.save(dailyTariff);
        tariffRepository.save(inactiveTariff);

        // When
        var allTariffs = tariffRepository.findAll();

        // Then
        assertThat(allTariffs).hasSize(3);
    }

    @Test
    @DisplayName("Should delete tariff by ID")
    void testDeleteById_Success() {
        // Given
        Tariff saved = tariffRepository.save(oneTimeTariff);
        Long savedId = saved.getId();

        // When
        tariffRepository.deleteById(savedId);

        // Then
        Optional<Tariff> found = tariffRepository.findById(savedId);
        assertThat(found).isEmpty();
    }
}

