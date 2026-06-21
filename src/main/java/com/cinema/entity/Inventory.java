package com.cinema.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @Column(name = "type_of_inventory", nullable = false, length = 100)
    private String typeOfInventory;

    @Column(nullable = false)
    private Integer quantity;

    public Inventory() {
    }

    public Inventory(Camp camp, String typeOfInventory, Integer quantity) {
        this.camp = camp;
        this.typeOfInventory = typeOfInventory;
        this.quantity = quantity;
    }

    public Inventory(Integer id, Camp camp, String typeOfInventory, Integer quantity) {
        this.id = id;
        this.camp = camp;
        this.typeOfInventory = typeOfInventory;
        this.quantity = quantity;
    }


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Camp getCamp() { return camp; }
    public void setCamp(Camp camp) { this.camp = camp; }
    public String getTypeOfInventory() { return typeOfInventory; }
    public void setTypeOfInventory(String typeOfInventory) { this.typeOfInventory = typeOfInventory; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", camp=" + (camp != null ? camp.getId() : null) +
                ", typeOfInventory='" + typeOfInventory + '\'' +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory)) return false;
        Inventory inventory = (Inventory) o;
        return id != null && id.equals(inventory.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
