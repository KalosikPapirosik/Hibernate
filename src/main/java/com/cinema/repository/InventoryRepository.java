package com.cinema.repository;

import com.cinema.entity.Camp;
import com.cinema.entity.Inventory;

public class InventoryRepository extends GenericRepository<Inventory, Integer>{
    public InventoryRepository() {super(Inventory.class);}
}
