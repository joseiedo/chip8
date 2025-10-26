package br.com.joseiedo.chip8;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Chip8Test {

    @ParameterizedTest
    @CsvSource({
            "123, 1;2;3",
            "6, 0;0;6",
            "80, 0;8;0",
            "0, 0;0;0"
    })
    void shouldExtract3Digits(int number, String expectedStr) {
        int[] expected = Arrays.stream(expectedStr.split(";"))
                .mapToInt(Integer::parseInt)
                .toArray();

        int[] result = Chip8.extractDigits(number);

        Assertions.assertArrayEquals(expected, result);
    }
}