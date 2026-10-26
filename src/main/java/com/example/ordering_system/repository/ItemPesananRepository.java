package com.example.ordering_system.repository;

import com.example.ordering_system.entity.ItemPesanan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPesananRepository extends JpaRepository<ItemPesanan, String> {
}
