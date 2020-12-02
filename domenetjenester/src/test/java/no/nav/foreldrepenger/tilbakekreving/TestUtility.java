package no.nav.foreldrepenger.tilbakekreving;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class TestUtility {

    private static final AtomicLong GEN_SAK_ID = new AtomicLong(1000000L);
    private static final AtomicLong GEN_UID = new AtomicLong(300000L);
    private static final AtomicLong GEN_SAK_NR = new AtomicLong(500000L);
    private static final AtomicLong GEN_EBEH_ID = new AtomicLong(999950L);

    private static final BehandlingType BEHANDLING_TYPE = BehandlingType.TILBAKEKREVING;

    private BehandlingTjeneste behandlingTjeneste;

    public TestUtility(BehandlingTjeneste behandlingTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
    }

    public AktørId genererAktørId() {
        return new AktørId(GEN_UID.getAndIncrement());
    }

    /**
     * Oppretter behandling med genererte data
     * @return Klasse med genererte detaljer om saken
     */
    public SakDetaljer opprettFørstegangsBehandling(AktørId aktørId) {
        // generer data
        long fagsakId = genererFagsakId();
        long eksBehId = genererEksternBehandlingId();
        Henvisning henvisning = Henvisning.fraEksternBehandlingId(eksBehId);
        Saksnummer saksnummer = genererSaksnummer();
        UUID eksternUuid = genererEksternUuid();

        long intBehId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternUuid, henvisning, aktørId,FagsakYtelseType.FORELDREPENGER,BEHANDLING_TYPE);

        Behandling behandling = behandlingTjeneste.hentBehandling(intBehId);

        // opprett SakDetaljer
        return new SakDetaljer(fagsakId, saksnummer, intBehId, henvisning, eksternUuid, aktørId, BEHANDLING_TYPE, behandling);
    }

    public Optional<Personinfo> lagPersonInfo(AktørId aktørId) {
        Personinfo personinfo = Personinfo.builder()
                .medAktørId(aktørId)
                .medFødselsdato(LocalDate.now().minusYears(20))
                .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
                .medPersonIdent(new PersonIdent(aktørId.getId()))
                .medNavn("testnavn")
                .build();
        return Optional.of(personinfo);
    }

    public UUID genererEksternUuid() {
        byte[] data = new byte[16];
        Random r = new Random();
        r.nextBytes(data);
        return UUID.nameUUIDFromBytes(data);
    }

    private Long genererFagsakId() {
        return GEN_SAK_ID.getAndIncrement();
    }

    private Saksnummer genererSaksnummer() {
        Long nr = GEN_SAK_NR.getAndIncrement();
        return new Saksnummer(String.valueOf(nr));
    }

    private Long genererEksternBehandlingId() {
        return GEN_EBEH_ID.getAndIncrement();
    }

    public static class SakDetaljer {
        private Long fagsakId;
        private Saksnummer saksnummer;
        private Long internBehandlingId;
        private Henvisning henvisning;
        private UUID eksternUuid;
        private AktørId aktørId;
        private BehandlingType behandlingType;
        private Behandling behandling;

        public SakDetaljer(Long fagsakId, Saksnummer saksnummer, Long internBehandlingId, Henvisning henvisning,
                           UUID eksternUuid, AktørId aktørId, BehandlingType behandlingType, Behandling behandling) {
            this.fagsakId = fagsakId;
            this.saksnummer = saksnummer;
            this.internBehandlingId = internBehandlingId;
            this.henvisning=henvisning;
            this.eksternUuid = eksternUuid;
            this.aktørId = aktørId;
            this.behandlingType = behandlingType;
            this.behandling = behandling;
        }

        public Long getFagsakId() {
            return fagsakId;
        }

        public Saksnummer getSaksnummer() {
            return saksnummer;
        }

        public Long getInternBehandlingId() {
            return internBehandlingId;
        }

        public Henvisning getHenvisning() {
            return henvisning;
        }

        public UUID getEksternUuid() {
            return eksternUuid;
        }

        public AktørId getAktørId() {
            return aktørId;
        }

        public BehandlingType getBehandlingType() {
            return behandlingType;
        }

        public Behandling getBehandling() {
            return behandling;
        }
    }
}
