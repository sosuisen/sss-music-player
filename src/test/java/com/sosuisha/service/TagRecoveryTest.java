package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TagRecoveryTest {
    @Test
    @DisplayName("Shift_JISのバイト列をLatin-1として誤復号した文字列は、誤復号と判定される")
    void a_string_of_sjis_bytes_misdecoded_as_latin1_is_detected() {
        var garbled = new String(
            "アンテナスイッチ".getBytes(Charset.forName("MS932")),
            StandardCharsets.ISO_8859_1
        );

        assertTrue(TagRecovery.isSjisMisdecodedAsLatin1(garbled));
    }

    @Test
    @DisplayName("ASCIIだけの文字列は、誤復号と判定されない")
    void an_ascii_only_string_is_not_detected() {
        assertFalse(TagRecovery.isSjisMisdecodedAsLatin1("One Day A Happy Day!"));
    }

    @Test
    @DisplayName("Caféのような正しいLatin-1の西欧文字列は、誤復号と判定されない")
    void a_correct_latin1_western_string_is_not_detected() {
        assertFalse(TagRecovery.isSjisMisdecodedAsLatin1("Café"));
    }

    @Test
    @DisplayName("Latin-1として誤復号された文字列から、元のShift_JISの文字列を復元する")
    void redecodes_a_latin1_misdecoded_string_as_sjis() {
        var garbled = new String(
            "アンテナスイッチ".getBytes(Charset.forName("MS932")),
            StandardCharsets.ISO_8859_1
        );

        assertEquals("アンテナスイッチ", TagRecovery.redecodeLatin1AsSjis(garbled));
    }
}
