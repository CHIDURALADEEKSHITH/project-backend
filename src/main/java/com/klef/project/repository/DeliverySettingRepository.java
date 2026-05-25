package com.klef.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.klef.project.entity.DeliverySetting;

public interface DeliverySettingRepository extends JpaRepository<DeliverySetting, Integer>
{
}