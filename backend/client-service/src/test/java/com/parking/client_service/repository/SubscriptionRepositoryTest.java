package com.parking.client_service.repository;
import com.parking.common.entity.Client;
import com.parking.common.entity.Subscription;
import com.parking.common.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
/**
 * DataJpaTest for SubscriptionRepository (Issue #72).
 * Uses H2 in-memory DB; schema created by Hibernate from entities.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SubscriptionRepository - active subscription lookup")
class SubscriptionRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    private static final String ACTIVE_PLATE   = "AA1234BB";
    private static final String INACTIVE_PLATE = "BB5678CC";
    private static final String UNKNOWN_PLATE  = "ZZ0000ZZ";
    @BeforeEach
    void insertTestData() {
        // Client 1 - has active subscription
        Client client1 = new Client();
        client1.setFullName("Subscriber Test");
        client1.setPhoneNumber("+380501112233");
        client1.setEmail("sub@parking.com");
        client1.setRegisteredAt(LocalDateTime.now());
        em.persist(client1);
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setLicensePlate(ACTIVE_PLATE);
        vehicle1.setClient(client1);
        vehicle1.setIsAllowed(true);
        em.persist(vehicle1);
        Subscription activeSub = new Subscription();
        activeSub.setClient(client1);
        activeSub.setStartDate(LocalDateTime.now().minusDays(10));
        activeSub.setEndDate(LocalDateTime.now().plusDays(355));
        activeSub.setType("ANNUAL");
        activeSub.setIsActive(true);
        em.persist(activeSub);
        // Client 2 - has expired/inactive subscription
        Client client2 = new Client();
        client2.setFullName("Guest Test");
        client2.setPhoneNumber("+380679998877");
        client2.setEmail("guest@parking.com");
        client2.setRegisteredAt(LocalDateTime.now());
        em.persist(client2);
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setLicensePlate(INACTIVE_PLATE);
        vehicle2.setClient(client2);
        vehicle2.setIsAllowed(true);
        em.persist(vehicle2);
        // Expired subscription (end_date in the past)
        Subscription expiredSub = new Subscription();
        expiredSub.setClient(client2);
        expiredSub.setStartDate(LocalDateTime.now().minusYears(2));
        expiredSub.setEndDate(LocalDateTime.now().minusDays(1));
        expiredSub.setType("ANNUAL");
        expiredSub.setIsActive(true);
        em.persist(expiredSub);
        // Inactive subscription (is_active = false)
        Subscription inactiveSub = new Subscription();
        inactiveSub.setClient(client2);
        inactiveSub.setStartDate(LocalDateTime.now().minusMonths(1));
        inactiveSub.setEndDate(LocalDateTime.now().plusMonths(11));
        inactiveSub.setType("ANNUAL");
        inactiveSub.setIsActive(false);
        em.persist(inactiveSub);
        em.flush();
    }
    @Test
    @DisplayName("Returns active subscription for plate AA1234BB")
    void findActiveByLicensePlate_activeSubscription_returnsPresent() {
        Optional<Subscription> result =
                subscriptionRepository.findActiveByLicensePlate(ACTIVE_PLATE);
        assertThat(result).isPresent();
        assertThat(result.get().getIsActive()).isTrue();
        assertThat(result.get().getEndDate()).isAfter(LocalDateTime.now());
    }
    @Test
    @DisplayName("Returns empty for expired subscription")
    void findActiveByLicensePlate_expiredSubscription_returnsEmpty() {
        // Only expired/inactive subs exist for INACTIVE_PLATE
        Optional<Subscription> result =
                subscriptionRepository.findActiveByLicensePlate(INACTIVE_PLATE);
        assertThat(result).isEmpty();
    }
    @Test
    @DisplayName("Returns empty for completely unknown license plate")
    void findActiveByLicensePlate_unknownPlate_returnsEmpty() {
        Optional<Subscription> result =
                subscriptionRepository.findActiveByLicensePlate(UNKNOWN_PLATE);
        assertThat(result).isEmpty();
    }
}