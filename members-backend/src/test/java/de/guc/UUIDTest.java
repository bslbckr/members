package de.guc;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UUIDTest {

    private final static String input = "df8ea850-52a3-4856-aea0-79361d48ff8c";

    @Test
    public void convertInputUUID() {
        final var uuid = UUID.fromString(input);
        Assertions.assertNotNull(uuid);

        final long least = uuid.getLeastSignificantBits();
        final long most = uuid.getMostSignificantBits();
        final var buffer = ByteBuffer.allocate(16);
        buffer.putLong(least);
        buffer.putLong(most);
        
        //Assertions.assertEquals(input, new String(buffer.array()));
    }
}
