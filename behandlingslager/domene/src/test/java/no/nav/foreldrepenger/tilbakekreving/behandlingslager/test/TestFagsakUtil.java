package no.nav.foreldrepenger.tilbakekreving.behandlingslager.test;

import java.util.concurrent.atomic.AtomicLong;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class TestFagsakUtil {

    private static final AtomicLong FAKE_UID = new AtomicLong(10000099L);
    private static final AtomicLong FAKE_FID = new AtomicLong(500011L);
    private static final AtomicLong FAKE_SNR = new AtomicLong(2000001L);
    public static final Saksnummer SAKSNUMMER = new Saksnummer("124355");

    public static Fagsak opprettFagsak() {
        return opprettFagsak(genererFagsakId(), genererSaksnummer(), genererBruker());
    }

    public static Fagsak opprettFagsak(NavBruker bruker) {
        return opprettFagsak(genererFagsakId(), SAKSNUMMER, bruker);
    }

    public static Fagsak opprettFagsak(Saksnummer saksnummer, NavBruker bruker) {
        return opprettFagsak(genererFagsakId(), saksnummer, bruker);
    }

    public static Fagsak opprettFagsak(long fagsakId, Saksnummer saksnummer, NavBruker bruker) {
        return Fagsak.opprettNy(fagsakId, saksnummer, bruker);
    }

    public static NavBruker genererBruker() {
        AktørId aktørId = new AktørId(nyId());
        return NavBruker.opprettNy(aktørId, Språkkode.nb);
    }

    public static Long genererFagsakId() {
        return FAKE_FID.getAndIncrement();
    }

    public static Saksnummer genererSaksnummer() {
        Long snr = FAKE_SNR.getAndIncrement();
        return new Saksnummer(snr.toString());
    }

    private static Long nyId() {
        return FAKE_UID.getAndIncrement();
    }

}
