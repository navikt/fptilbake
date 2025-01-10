package no.nav.foreldrepenger.tilbakekreving.k9sak.klient;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

public class K9HenvisningKonverterer {

    public static Henvisning uuidTilHenvisning(UUID uuid) {
        return uuidTilHenvisning(uuid, null);
    }

    public static Henvisning uuidTilHenvisning(UUID uuid, Integer sekvensnummer) {
        ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        String base64encodedWithPadding = Base64.getEncoder().encodeToString(buffer.array());
        int paddingStart = base64encodedWithPadding.indexOf("=");
        String henvisningString = paddingStart == -1 ? base64encodedWithPadding : base64encodedWithPadding.substring(0, paddingStart);
        if (sekvensnummer != null) {
            henvisningString += "=" + sekvensnummer;
        }
        return new Henvisning(henvisningString);
    }

    public static UUID henvisningTilUuid(Henvisning henvisning) {
        String henvisningStreng = henvisning.getVerdi();
        String henvisningUuidDel = henvisningStreng.contains("=") ? henvisningStreng.split("=")[0] : henvisningStreng;

        ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
        buffer.put(Base64.getDecoder().decode(henvisningUuidDel));
        buffer.flip();
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

}
