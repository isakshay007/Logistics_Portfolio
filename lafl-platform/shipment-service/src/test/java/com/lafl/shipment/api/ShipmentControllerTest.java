package com.lafl.shipment.api;

import com.lafl.shipment.domain.Shipment;
import com.lafl.shipment.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentService shipmentService;

    @Test
    void getShipmentReturns200WhenFound() throws Exception {
        Shipment shipment = new Shipment();
        shipment.setReference("LAFL-10001");
        shipment.setStatus("On Schedule");

        when(shipmentService.findByReference("LAFL-10001")).thenReturn(Optional.of(shipment));

        mockMvc.perform(get("/api/v1/shipments/LAFL-10001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shipment.reference").value("LAFL-10001"));
    }

    @Test
    void trackShipmentReturns200WhenFound() throws Exception {
        Shipment shipment = new Shipment();
        shipment.setReference("LAFL-24017");
        shipment.setStatus("Delayed");

        when(shipmentService.findByReference("LAFL-24017")).thenReturn(Optional.of(shipment));

        mockMvc.perform(get("/api/v1/shipments/track").param("reference", "LAFL-24017"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shipment.reference").value("LAFL-24017"));
    }

    @Test
    void patchShipmentReturns404WhenMissing() throws Exception {
        when(shipmentService.updateStatus(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/shipments/LAFL-404/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"status\":\"Delayed\"," +
                    "\"currentLocation\":\"Hub\"," +
                    "\"progress\":44" +
                    "}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Shipment reference not found."));
    }
}
