package com.cinema.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class HousingCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    public HousingCondition() {
    }

    public HousingCondition(String name) {
        this.name = name;
    }

    public HousingCondition(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}


    @Override
    public String toString() {
        return "HousingCondition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HousingCondition)) return false;
        HousingCondition that = (HousingCondition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}