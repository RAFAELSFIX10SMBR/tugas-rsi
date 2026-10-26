package com.example.ordering_system.repository;

import com.example.orderingsystem.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MenuRepository extends JpaRepository<Menu, String> {}
