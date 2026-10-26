package com.example.ordering_system.controller;

import com.example.ordering_system.dto.request.BatalTransaksiRequest;
import com.example.ordering_system.dto.request.CheckoutRequest;
import com.example.ordering_system.dto.response.ApiResponse;
import com.example.ordering_system.dto.response.CheckoutResponse;
import com.example.ordering_system.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /** Merepresentasikan method: +prosesCheckout(idPelanggan, keranjangData) */
    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponse>> prosesCheckout(
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = checkoutService.prosesCheckout(request);
        return new ResponseEntity<>(
                ApiResponse.success("Checkout dan pembayaran berhasil diproses", response),
                HttpStatus.CREATED);
    }

    /** Merepresentasikan method: +batalkanTransaksi(alasan) */
    @PostMapping("/{idPesanan}/batal")
    public ResponseEntity<ApiResponse<Void>> batalkanTransaksi(
            @PathVariable String idPesanan,
            @Valid @RequestBody BatalTransaksiRequest request) {
        checkoutService.batalkanTransaksi(idPesanan, request.getAlasan());
        return ResponseEntity.ok(
                ApiResponse.success("Transaksi berhasil dibatalkan: " + request.getAlasan(), null));
    }
}
