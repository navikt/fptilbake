package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;

class KontekstUtilTest {

    @BeforeEach
    void setUp() {
        System.setProperty("app.name", "fptilbake");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("app.name");
        KontekstHolder.fjernKontekst();
    }

    @Test
    void kanSaksbehandle_ok_innlogget_saksbehandler() {
        KontekstHolder.setKontekst(getRequestKontekstFor(AnsattGruppe.SAKSBEHANDLER));
        assertThat(KontekstUtil.kanSaksbehandle()).isTrue();
    }

    @Test
    void kanSaksbehandle_nok_innlogget_veileder() {
        KontekstHolder.setKontekst(getRequestKontekstFor(AnsattGruppe.VEILEDER));
        assertThat(KontekstUtil.kanSaksbehandle()).isFalse();
    }

    @Test
    void kanSaksbehandle_nok_innlogget_system() {
        KontekstHolder.setKontekst(RequestKontekst.forProsesstaskUtenSystembruker());
        assertThat(KontekstUtil.kanSaksbehandle()).isFalse();
    }

    @Test
    void kanSaksbehandle_nok_uinlogget() {
        KontekstHolder.setKontekst(RequestKontekst.ikkeAutentisertRequest("consumerId"));
        assertThat(KontekstUtil.kanSaksbehandle()).isFalse();
    }

    @Test
    void kanSaksbehandle_nok_uten_kontekst() {
        assertThat(KontekstUtil.kanSaksbehandle()).isFalse();
    }

    private static @NotNull RequestKontekst getRequestKontekstFor(AnsattGruppe saksbehandler) {
        return RequestKontekst.forRequest("testUid", "kompaktUid", IdentType.InternBruker, null, UUID.randomUUID(), Set.of(
            saksbehandler));
    }
}
