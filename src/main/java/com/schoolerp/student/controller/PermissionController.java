//package com.schoolerp.student.controller;
//
//
//import com.schoolerp.student.dto.PermissionRequest;
//import com.schoolerp.student.dto.PermissionResponse;
//import com.schoolerp.student.service.PermissionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/permissions")
//@RequiredArgsConstructor
//public class PermissionController {
//
//    private final PermissionService service;
//
//    @GetMapping
//    public Page<PermissionResponse> listByRole(@RequestParam Long roleId,
//                                               @RequestParam(defaultValue = "0") int page,
//                                               @RequestParam(defaultValue = "50") int size) {
//        return service.listByRole(roleId, page, size);
//    }
//
//    @PostMapping("/{roleId}")
//    @ResponseStatus(HttpStatus.CREATED)
//    public void saveAll(@PathVariable Long roleId, @RequestBody List<PermissionRequest> list) {
//        service.saveAll(roleId, list);
//    }
//}
