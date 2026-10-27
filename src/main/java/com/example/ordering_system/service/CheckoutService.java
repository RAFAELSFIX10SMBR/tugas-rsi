package com.example.ordering_system.service;

import com.example.ordering_system.dto.request.CheckoutRequest;
import com.example.ordering_system.dto.response.CheckoutResponse;
public interface CheckoutService {
 CheckoutResponse prosesCheckout(CheckoutRequest request);
 void batalkanTransaksi(String idPesanan, String alasan);
}
