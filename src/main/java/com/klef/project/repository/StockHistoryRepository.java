package com.klef.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.klef.project.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer>
{
}