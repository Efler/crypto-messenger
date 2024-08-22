package org.eflerrr.server.service;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Random;

@Service
@NoArgsConstructor
public class DiffieHellmanService {

    private final int[] primitiveVals = new int[]{2, 3, 5, 7, 11, 13, 17};
    private final Random random = new Random();

    public Pair<BigInteger, BigInteger> generateParams(int bitLength) {
        BigInteger g = BigInteger.valueOf(primitiveVals[random.nextInt(7)]);
        BigInteger p;
        do {
            p = BigInteger.probablePrime(bitLength, random);
        } while (!g.modPow(p.subtract(BigInteger.ONE), p).equals(BigInteger.ONE));
        return Pair.of(g, p);
    }

}
