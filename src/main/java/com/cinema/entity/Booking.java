package com.cinema.entity;


import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_id", nullable = false)
    private Housing housing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "additional_services_id")
    private AdditionalServices additionalService;

    @Column(name = "date_of_start", nullable = false)
    private OffsetDateTime dateOfStart;

    @Column(name = "date_of_end", nullable = false)
    private OffsetDateTime dateOfEnd;

    @Column(name = "date_of_service")
    private OffsetDateTime dateOfService;

    // ─── Конструкторы ─────────────────────────────────────
    public Booking() {
    }

    public Booking(Guest guest, Housing housing, AdditionalServices additionalService,
                   OffsetDateTime dateOfStart, OffsetDateTime dateOfEnd, OffsetDateTime dateOfService) {
        this.guest = guest;
        this.housing = housing;
        this.additionalService = additionalService;
        this.dateOfStart = dateOfStart;
        this.dateOfEnd = dateOfEnd;
        this.dateOfService = dateOfService;
    }

    // ─── Геттеры и Сеттеры ────────────────────────────────
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Housing getHousing() { return housing; }
    public void setHousing(Housing housing) { this.housing = housing; }

    public AdditionalServices getAdditionalService() { return additionalService; }
    public void setAdditionalService(AdditionalServices additionalService) { this.additionalService = additionalService; }

    public OffsetDateTime getDateOfStart() { return dateOfStart; }
    public void setDateOfStart(OffsetDateTime dateOfStart) { this.dateOfStart = dateOfStart; }

    public OffsetDateTime getDateOfEnd() { return dateOfEnd; }
    public void setDateOfEnd(OffsetDateTime dateOfEnd) { this.dateOfEnd = dateOfEnd; }

    public OffsetDateTime getDateOfService() { return dateOfService; }
    public void setDateOfService(OffsetDateTime dateOfService) { this.dateOfService = dateOfService; }

    // ─── Утилиты ──────────────────────────────────────────
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", guestId=" + (guest != null ? guest.getId() : null) +
                ", housingId=" + (housing != null ? housing.getId() : null) +
                ", serviceId=" + (additionalService != null ? additionalService.getId() : null) +
                ", start=" + dateOfStart +
                ", end=" + dateOfEnd +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return id != null && id.equals(booking.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
