package com.cinema.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 32)
    private String lastname;

    @Column(nullable = false, length = 32)
    private String firstname;

    @Column(nullable = true, length = 32)
    private String patronymic;

    @Column(nullable = false, unique = true, length = 20)
    private String document;

    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    protected Guest() {};

    public Guest(String lastname, String firstname, String patronymic, String document) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.patronymic = patronymic;
        this.document = document;
    }

    public Guest(String lastname, String firstname, String document) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.document = document;
    }

    public Integer getId() {return id;}
    public String getLastname() {return lastname;}
    public String getFirstname() {return firstname;}
    public String getPatronymic() {return patronymic;}
    public String getDocument() { return document; }
    public void setId(Integer id) {this.id = id;}
    public void setLastname(String lastname) {this.lastname = lastname;}
    public void setFirstname(String firstname) {this.firstname = firstname;}
    public void setPatronymic(String patronymic) {this.patronymic = patronymic;}
    public void setDocument(String document) {this.document = document;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(id, guest.id) && Objects.equals(lastname, guest.lastname) && Objects.equals(firstname, guest.firstname) && Objects.equals(patronymic, guest.patronymic) && Objects.equals(document, guest.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastname, firstname, patronymic, document);
    }

    @Override
    public String toString() {
        return String.format("Guest{id=%d, '%s', %s, document=%s}", id, firstname, lastname, document);
    }
}
