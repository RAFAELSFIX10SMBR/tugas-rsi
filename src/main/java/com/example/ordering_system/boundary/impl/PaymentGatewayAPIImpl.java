package com.example.ordering_system.boundary.impl;

import com.example.ordering_system.boundary.PaymentGatewayAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentGatewayAPIImpl implements PaymentGatewayAPI {

    @Value("${payment.gateway.endpoint-url}")
    private String endpointUrl;

    @Value("${payment.gateway.api-key}")
    private String apiKey;

    @Override
    public String mintaTransaksiPembayaran(double total, String tokenKartu) {
        // Simulasi komunikasi HTTP ke endpoint gateway pihak ketiga
        System.out.println("== [BOUNDARY LOG] Mengirim Transaksi ke Gateway ==");
        System.out.println("URL: " + endpointUrl);
        System.out.println("API Key: " + apiKey.substring(0, 7) + "********");
        System.out.println("Charge Total: Rp" + total + " | Card Token: " + tokenKartu);

        // Validasi token tiruan: "invalid_card" akan ditolak
        if ("invalid_card".equalsIgnoreCase(tokenKartu)) {
            throw new RuntimeException(
                    "Penolakan dari Gateway Pembayaran: Saldo tidak cukup atau token kartu tidak sah");
        }

        // Kode referensi transaksi eksternal yang unik
        return "PG-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
