package com.ctx142.tool;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDto {
    private String balance;

    private String wStatus;

    private String walletIdOrName;

    private String realTimeBalance;

    private Boolean success;
}
