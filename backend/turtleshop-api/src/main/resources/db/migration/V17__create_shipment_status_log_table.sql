CREATE TABLE IF NOT EXISTS SHIPMENT_STATUS_LOG (
    log_id SERIAL PRIMARY KEY,
    shipment_id INT NOT NULL,
    status TEXT NOT NULL,
    status_change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_shipment_status_log FOREIGN KEY (shipment_id) REFERENCES SHIPMENT(shipment_id)
);