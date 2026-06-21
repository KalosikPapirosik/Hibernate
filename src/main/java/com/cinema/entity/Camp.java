package com.cinema.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "camp")
public class Camp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @OneToMany(mappedBy = "camp", fetch = FetchType.LAZY)
    private List<Housing> housing = new ArrayList<>();

    @OneToMany(mappedBy = "camp", fetch = FetchType.LAZY)
    private List<Inventory> inventory = new ArrayList<>();

    public Camp() {
    }

    public Camp(String location) {
        this.location = location;
    }

    public Camp(Integer id, String location) {
        this.id = id;
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Camp{" +
                "id=" + id +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Camp)) return false;
        Camp camp = (Camp) o;
        return id != null && id.equals(camp.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // Безопасно для прокси-объектов Hibernate
    }

    public List<Inventory> getInventory() {
        return inventory;
    }
}
