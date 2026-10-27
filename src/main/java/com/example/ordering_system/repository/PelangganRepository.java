package com.example.ordering_system.repository;

import com.example.ordering_system.entity.Pelanggan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PelangganRepository extends JpaRepository<Pelanggan, String> {}
