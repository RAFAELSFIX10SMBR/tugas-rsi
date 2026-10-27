package com.example.ordering_system.dto.response;

import com.example.ordering_system.entity.StatusPesanan;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {
 private String idPesanan;
 private String idPembayaran;
 private double totalBiaya;
 private StatusPesanan statusPesanan;
 private String statusPembayaran;
 private String kodeReferensi;
 private LocalDateTime tanggal;
}
