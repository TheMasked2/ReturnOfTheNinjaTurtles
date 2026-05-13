package org.turtleshop.api.modules.shipment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.shipment.dto.CreateShipmentRequest;
import org.turtleshop.api.modules.shipment.dto.CreateShipmentStatusLogRequest;
import org.turtleshop.api.modules.shipment.dto.ShipmentResponse;
import org.turtleshop.api.modules.shipment.dto.ShipmentStatusLogResponse;
import org.turtleshop.api.modules.shipment.dto.UpdateShipmentLogNotesRequest;
import org.turtleshop.api.modules.shipment.dto.UpdateShipmentMethodRequest;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;
import org.turtleshop.api.modules.shipment.model.Shipment;
import org.turtleshop.api.modules.shipment.model.ShipmentStatusLog;
import org.turtleshop.api.modules.shipment.repository.ShipmentAccess;
import org.turtleshop.api.modules.shipment.repository.ShipmentStatusLogAccess;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentAccess shipmentAccess;
    private final ShipmentStatusLogAccess shipmentStatusLogAccess;

    // Create Shipment
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        int shipmentId = shipmentAccess.createShipment(request.getOrderId(), request.getShipmentMethod(), request.getShippingAddress());
        shipmentStatusLogAccess.createShipmentStatusLog(shipmentId, ShipmentStatus.SHIPPED);
        Shipment shipment = shipmentAccess.getShipmentById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Shipment could not be created"));
        return mapToShipmentResponse(shipment);
    }

    // Get Shipment for a ShipmentId
    public ShipmentResponse getShipmentByShipmentId(int shipmentId) {
        Shipment existingShipment = shipmentAccess.getShipmentById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment exists for this shipment id"));
        return mapToShipmentResponse(existingShipment);
    }

    // Get Shipment for an OrderId
    public ShipmentResponse getShipmentByOrderId(int orderId) {
        Shipment existingShipment = shipmentAccess.getShipmentByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment exists for this order id"));
        return mapToShipmentResponse(existingShipment);
    }

    // Get ShipmentStatusLog by LogId
    public ShipmentStatusLogResponse getShipmentStatusLogById(int logId) {
        ShipmentStatusLog shipmentStatusLog = shipmentStatusLogAccess.getShipmentStatusLog(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment status log exists for this log id"));
        return mapToShipmentStatusLogResponse(shipmentStatusLog);
    }

    // Get all ShipmentStatusLogs for a Shipment
    public List<ShipmentStatusLogResponse> getAllShipmentStatusLogsOfShipment(int shipmentId) {
        Shipment existingShipment = shipmentAccess.getShipmentById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment exists for this shipment id"));
        List<ShipmentStatusLog> shipmentStatusLogs =
                shipmentStatusLogAccess.getAllShipmentStatusLogsOfShipment(existingShipment.getShipmentId());
        if (shipmentStatusLogs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No shipment status logs found for this shipment");
        }
        return shipmentStatusLogs.stream().map(this::mapToShipmentStatusLogResponse).toList();
    }

    // Create ShipmentStatusLog for Shipment
    public ShipmentStatusLogResponse createShipmentStatusLog(int shipmentId, CreateShipmentStatusLogRequest request) {
        shipmentAccess.getShipmentById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment exists for this shipment id"));
        int logId = shipmentStatusLogAccess.createShipmentStatusLog(
                shipmentId,
                request.getStatus()
        );
        ShipmentStatusLog shipmentStatusLog = shipmentStatusLogAccess.getShipmentStatusLog(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Shipment status log could not be created"));
        return mapToShipmentStatusLogResponse(shipmentStatusLog);
    }

    // Update Shipment Method
    public void updateShipmentMethod(int shipmentId, UpdateShipmentMethodRequest request) {
        shipmentAccess.getShipmentById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment exists for this shipment id"));
        shipmentAccess.updateShipmentMethod(shipmentId, request.getShipmentMethod());
    }

    // Update ShipmentStatusLog Notes
    public void updateShipmentStatusLogNotes(int logId, UpdateShipmentLogNotesRequest request) {
        shipmentStatusLogAccess.getShipmentStatusLog(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No shipment status log exists for this log id"));
        shipmentStatusLogAccess.updateNotes(logId, request.getNotes());
    }

    // HELPER: Maps the Model to the Response DTO
    public ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrderId())
                .shipmentMethod(shipment.getShipmentMethod())
                .shippingAddress(shipment.getShippingAddress())
                .build();
    }

    // HELPER: Maps the Model to the Response DTO
    public ShipmentStatusLogResponse mapToShipmentStatusLogResponse(ShipmentStatusLog shipmentStatusLog) {
        return ShipmentStatusLogResponse.builder()
                .logId(shipmentStatusLog.getLogId())
                .shipmentId(shipmentStatusLog.getShipmentId())
                .status(shipmentStatusLog.getStatus())
                .statusChangeDate(shipmentStatusLog.getStatusChangeDate())
                .notes(shipmentStatusLog.getNotes())
                .build();
    }
}