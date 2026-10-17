package com.neuromart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuromart.dto.PaymentProcessRequest;
import com.neuromart.dto.PaymentVerificationRequest;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PaymentSerializationTest {

    private PaymentService paymentService;
    private RazorpayClient razorpayClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        razorpayClient = Mockito.mock(RazorpayClient.class);
        paymentService = new PaymentService(razorpayClient);
        ReflectionTestUtils.setField(paymentService, "keyId", "rzp_test_RDd2Pd7LEYMgKU");
        ReflectionTestUtils.setField(paymentService, "keySecret", "9GS7fHiRv102dxsUFEEZvPC4");
        objectMapper = new ObjectMapper();
    }

    @Test
    void testProcessPaymentFallbackSerialization() throws Exception {
        // Razorpay client order create will fail or throw exception -> triggers fallback
        when(razorpayClient.orders).thenReturn(Mockito.mock(OrderClient.class));
        when(razorpayClient.orders.create(any(JSONObject.class))).thenThrow(new RuntimeException("Simulated Razorpay failure"));

        Map<String, Object> response = paymentService.processPayment(new PaymentProcessRequest(250.0));
        assertNotNull(response);
        assertTrue((Boolean) response.get("success"));

        // Must be serializable by Jackson without throwing InvalidDefinitionException
        String json = objectMapper.writeValueAsString(response);
        assertTrue(json.contains("\"id\":\"order_"));
        assertTrue(json.contains("\"amount\":25000"));
    }

    @Test
    void testGetApiKey() throws Exception {
        Map<String, String> keyMap = paymentService.getApiKey();
        assertEquals("rzp_test_RDd2Pd7LEYMgKU", keyMap.get("key"));
        String json = objectMapper.writeValueAsString(keyMap);
        assertTrue(json.contains("rzp_test_RDd2Pd7LEYMgKU"));
    }

    @Test
    void testVerifyPayment() throws Exception {
        PaymentVerificationRequest req = new PaymentVerificationRequest("pay_123", "order_456", "invalid_sig");
        Map<String, Object> result = paymentService.verifyPayment(req);
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Payment verification failed", result.get("message"));

        String json = objectMapper.writeValueAsString(result);
        assertNotNull(json);
    }
}
