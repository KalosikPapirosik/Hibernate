package com.cinema.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Housing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private TypeOfHousing type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @Column(name = "comfort_lvl", nullable = false)
    private String comfortLvl;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_condition_id", nullable = false)
    private HousingCondition condition;

    @OneToMany(mappedBy = "housing", fetch = FetchType.LAZY)
    private List<Booking> booking = new ArrayList<>();

    public Housing() {
    }

    public Housing(TypeOfHousing type, Camp camp, String comfortLvl, Integer capacity, BigDecimal cost, HousingCondition condition) {
        this.type = type;
        this.camp = camp;
        this.comfortLvl = comfortLvl;
        this.capacity = capacity;
        this.cost = cost;
        this.condition = condition;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TypeOfHousing getType() {
        return type;
    }

    public void setType(TypeOfHousing type) {
        this.type = type;
    }

    public Camp getCamp() {
        return camp;
    }

    public void setCamp(Camp camp) {
        this.camp = camp;
    }

    public String getComfortLvl() {
        return comfortLvl;
    }

    public void setComfortLvl(String comfortLvl) {
        this.comfortLvl = comfortLvl;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public HousingCondition getCondition() {
        return condition;
    }

    public void setCondition(HousingCondition condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Housing{" +
                "id=" + id +
                ", type=" + (type != null ? type.getId() : null) +
                ", camp=" + (camp != null ? camp.getId() : null) +
                ", comfortLvl='" + comfortLvl + '\'' +
                ", capacity=" + capacity +
                ", cost=" + cost +
                ", condition=" + (condition != null ? condition.getId() : null) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Housing)) return false;
        Housing housing = (Housing) o;
        return id != null && id.equals(housing.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
