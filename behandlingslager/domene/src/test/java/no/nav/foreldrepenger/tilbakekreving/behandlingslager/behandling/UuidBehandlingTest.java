package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.Test;

public class UuidBehandlingTest {

    @Test
    public void testValidUuid() {
        String id = "9ec2fdb2-5c00-c8d7-e053-2d8c350a521e";

        UUID uuid = UUID.fromString(id);
        System.out.println(uuid.toString());
        System.out.println("Version: " + uuid.version());
        System.out.println("Variant: " + uuid.variant());
    }

    @Test
    public void testGenUuid() {
        int antKjøringer = 10000;
        System.out.println("Generer " + antKjøringer + " uuids.");
        for (int i = 0; i < antKjøringer; i++) {
            UUID genUuid = UUID.randomUUID();
            if (genUuid.version() != 4) {
            System.out.println(String.format("%d: %s, (%s, %s)", i, genUuid.toString(), genUuid.version(), genUuid.variant()));
            }
        }
        System.out.println("Sjekken gjort!");
    }
}
