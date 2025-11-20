package com.mykart.mykart.repository;

import com.mykart.mykart.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  InventoryRepository extends JpaRepository<Inventory, Long> {
}
