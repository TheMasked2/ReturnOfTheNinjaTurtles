package org.turtleshop.api.modules.shipment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.shipment.dto.CreateShipmentRequest;
import org.turtleshop.api.modules.shipment.dto.CreateShipmentStatusLogRequest;
import org.turtleshop.api.modules.shipment.dto.ShipmentResponse;
import org.turtleshop.api.modules.shipment.dto.ShipmentStatusLogResponse;
import org.turtleshop.api.modules.shipment.dto.UpdateShipmentLogNotesRequest;
import org.turtleshop.api.modules.shipment.dto.UpdateShipmentMethodRequest;
import org.turtleshop.api.modules.shipment.service.ShipmentService;

import java.util.List;

@RestController
@RequestMapping("api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@RequestBody CreateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.createShipment(request));
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable int shipmentId) {
        return ResponseEntity.ok(shipmentService.getShipmentByShipmentId(shipmentId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getShipmentByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderId(orderId));
    }

    @GetMapping("/logs/{logId}")
    public ResponseEntity<ShipmentStatusLogResponse> getShipmentStatusLog(@PathVariable int logId) {
        return ResponseEntity.ok(shipmentService.getShipmentStatusLogById(logId));
    }

    @GetMapping("/{shipmentId}/logs")
    public ResponseEntity<List<ShipmentStatusLogResponse>> getAllShipmentStatusLogs(@PathVariable int shipmentId) {
        return ResponseEntity.ok(shipmentService.getAllShipmentStatusLogsOfShipment(shipmentId));
    }

    @PostMapping("/{shipmentId}/logs")
    public ResponseEntity<ShipmentStatusLogResponse> createShipmentStatusLog(@PathVariable int shipmentId, @RequestBody CreateShipmentStatusLogRequest request) {
        return ResponseEntity.ok(shipmentService.createShipmentStatusLog(shipmentId, request));
    }

    @PatchMapping("/{shipmentId}/method")
    public ResponseEntity<String> updateShipmentMethod(@PathVariable int shipmentId, @RequestBody UpdateShipmentMethodRequest request) {
        shipmentService.updateShipmentMethod(shipmentId, request);
        return ResponseEntity.ok("Shipment method is updated");
    }

    @PatchMapping("/logs/{logId}/notes")
    public ResponseEntity<String> updateShipmentStatusLogNotes(@PathVariable int logId, @RequestBody UpdateShipmentLogNotesRequest request) {
        shipmentService.updateShipmentStatusLogNotes(logId, request);
        return ResponseEntity.ok("Shipment status log notes are updated");
    }
}