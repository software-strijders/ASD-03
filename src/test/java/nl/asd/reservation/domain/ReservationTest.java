package nl.asd.reservation.domain;

import nl.asd.reservation.port.adapter.FakeReservationRepository;
import nl.asd.shared.id.WorkplaceId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {
    private ReservationRepository repository = new FakeReservationRepository();

    /**
     * Helper method that reduces code bloat and increases readability
     * @return Time normalized to zero minutes
     */
    private LocalTime time() {
        return LocalTime.now().withMinute(0);
    }

    @BeforeEach
    public void initialize() {
        var targetReservation = new Reservation(new ReservationId(1L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(1L));
        targetReservation.reserveTimeslot(new Timeslot(LocalTime.now().withMinute(0), LocalTime.now().withMinute(30)), this.repository);
        this.repository.save(targetReservation);
        this.repository.save(new Reservation(new ReservationId(2L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(2L)));
        this.repository.save(new Reservation(new ReservationId(3L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(3L)));
        this.repository.save(new Reservation(new ReservationId(4L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(4L)));
    }

    @AfterEach
    public void cleanUp() {
        this.repository = new FakeReservationRepository();
    }

    @Test
    public void shouldCreateReservationCorrectly() {
    }

    @Test
    public void shouldThrowWhenReservationIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(null, LocalDate.now(), ReservationType.ONCE, new WorkplaceId(1)));
    }

    @Test
    public void shouldThrowWhenReservationDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(new ReservationId(1), null, ReservationType.ONCE, new WorkplaceId(1)));
    }

    @Test
    public void shouldThrowWhenReservationDateIsInThePast() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(new ReservationId(1), LocalDate.now().minusDays(1), ReservationType.ONCE, new WorkplaceId(1)));
    }

    @Test
    public void shouldThrowWhenReservationTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(new ReservationId(1), LocalDate.now(), null, new WorkplaceId(1)));
    }

    @Test
    public void shouldThrowWhenWorkplaceIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(new ReservationId(1), LocalDate.now(), ReservationType.ONCE, null));
    }

    @Test
    public void shouldCancelReservationCorectly() {
    }

    @Test
    public void shouldChangeReservationCorrectly() {
    }

    @Test
    public void shouldShowTotalReservationTimeCorrectlyWithOneTimeslot() {
        var reservation = new Reservation(new ReservationId(0L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(5));
        var timeslot = new Timeslot(time(), time().plusMinutes(30));
        reservation.reserveTimeslot(timeslot, this.repository);

        assertEquals(30, reservation.totalMinutesReserved());
    }

    @Test
    public void shouldShowTotalReservationTimeCorrectlyWithMultipleTimeslots() {
        var reservation = new Reservation(new ReservationId(0L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(5));
        var timeslot1 = new Timeslot(time(), time().plusMinutes(30));
        var timeslot2 = new Timeslot(time().plusMinutes(30), time().plusMinutes(60));
        var timeslot3 = new Timeslot(time().plusMinutes(60), time().plusMinutes(90));

        reservation.reserveTimeslots(List.of(timeslot1, timeslot2, timeslot3), this.repository);

        assertEquals(90, reservation.totalMinutesReserved());
    }

    @Test
    public void shouldThrowWhenReservingDuplicateTimeslots() {
        var reservation = new Reservation(new ReservationId(1L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(5));
        var timeslot = new Timeslot(time(), time().plusMinutes(30));

        reservation.reserveTimeslot(timeslot, this.repository);

        assertThrows(RuntimeException.class, () -> reservation.reserveTimeslot(timeslot, this.repository));
    }

    @Test
    public void shouldThrowWhenReservingATimeslotThatIsAlreadyInUse() {
        // workplaceid 1 is al in gebruik
        var reservation = new Reservation(new ReservationId(1L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(1));

        assertThrows(RuntimeException.class, () -> reservation.reserveTimeslot(
                new Timeslot(LocalTime.now(), LocalTime.now().plusMinutes(30)), this.repository));
    }

    @Test
    public void shouldNotThrowWhenReservingInATimeslotAfterOtherTimeslot() {
        // workplaceid 1 is al in gebruik, timeslot niet
        var reservation = new Reservation(new ReservationId(1L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(1));

        assertDoesNotThrow(() -> reservation.reserveTimeslot(
                new Timeslot(time().plusMinutes(60), time().plusMinutes(90)), this.repository));
    }

    @Test
    public void shouldNotThrowWhenReservingInATimeslotBeforeOtherTimeslot() {
        // workplaceid 1 is al in gebruik, timeslot niet
        var reservation = new Reservation(new ReservationId(1L), LocalDate.now().plusDays(1), ReservationType.ONCE, new WorkplaceId(1));

        assertDoesNotThrow(() -> reservation.reserveTimeslot(new Timeslot(time().minusMinutes(90), time().minusMinutes(60)), this.repository));
    }
}
