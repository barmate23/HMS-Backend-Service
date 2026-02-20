package com.schoolerp.staff.repository;


import com.schoolerp.staff.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {


    Route findByDriverId(Integer id);
}
