package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner;

public final record TransisjonIdentifikator(String id) {

    public static TransisjonIdentifikator forId(String id) {
        return new TransisjonIdentifikator(id);
    }

    public String getId() {
        return id;
    }

}
