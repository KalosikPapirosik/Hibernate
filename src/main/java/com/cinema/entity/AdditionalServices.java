package com.cinema.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class AdditionalServices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, length = 24)
    private String label;

    @Column(nullable = false)
    private BigDecimal cost;

    public AdditionalServices() {};

    public AdditionalServices(String label, BigDecimal cost) {
        this.label = label;
        this.cost = cost;
    }

    public Short getId() {return id;}

    public void setId(Short id) {this.id = id;}

    public BigDecimal getCost() {return cost;}

    public void setCost(BigDecimal cost) {this.cost = cost;}

    public String getLabel() {return label;}

    public void setLabel(String label) {this.label = label;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionalServices that = (AdditionalServices) o;
        return Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, cost);
    }

    @Override
    public String toString() {
        return "AdditionalService{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", cost=" + cost +
                '}';
    }
}
