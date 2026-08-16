package com.sosuisha.service;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Recovers tag text that was decoded with a wrong character encoding.
 * The typical case is a Japanese ID3 tag whose Shift_JIS bytes were
 * decoded as Latin-1 (ISO-8859-1).
 */
public class TagRecovery {
    private TagRecovery() {}

    /**
     * Returns whether the given string is Shift_JIS bytes misdecoded as
     * Latin-1 (ISO-8859-1).
     *
     * @param text string to check
     * @return true if the string is Shift_JIS bytes misdecoded as Latin-1
     * @throws NullPointerException if text is null
     */
    public static boolean isSjisMisdecodedAsLatin1(String text) {
        Objects.requireNonNull(text, "text must not be null");
        if (text.chars().noneMatch(c -> c >= 0x80 && c <= 0xFF)) { return false; }
        return decodesCleanlyAsSjis(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static boolean decodesCleanlyAsSjis(byte[] bytes) {
        var decoder = Charset.forName("MS932").newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}
