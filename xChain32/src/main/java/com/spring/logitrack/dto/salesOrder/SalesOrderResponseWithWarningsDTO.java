package com.spring.logitrack.dto.salesOrder;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesOrderResponseWithWarningsDTO {
    private SalesOrderResponseDTO order;
    private List<String> warnings;
}
