package org.eflerrr.client.dao;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Data
public class ClientDao {

    private String clientName;
    private Long clientId;
    private BigInteger privateKey;

}
