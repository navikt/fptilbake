package no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk;

import java.util.concurrent.atomic.AtomicLong;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class TestFagsakUtil {

    private static final AtomicLong FAKE_UID = new AtomicLong(10000099L);
    private static final AtomicLong FAKE_SNR = new AtomicLong(2000001L);
    public static final Saksnummer SAKSNUMMER = new Saksnummer("124355");

    public static Fagsak opprettFagsak() {
        return opprettFagsak(genererSaksnummer(), genererBruker());
    }

    public static Fagsak opprettFagsak(Saksnummer saksnummer, NavBruker bruker) {
        return Fagsak.opprettNy(saksnummer, bruker);
    }

    public static NavBruker genererBruker() {
        AktørId aktørId = new AktørId(nyId());
        return NavBruker.opprettNy(aktørId, Språkkode.nb);
    }

    public static Saksnummer genererSaksnummer() {
        Long snr = FAKE_SNR.getAndIncrement();
        return new Saksnummer(snr.toString());
    }

    private static Long nyId() {
        return FAKE_UID.getAndIncrement();
    }

}
