package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagBelop433Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagPeriode432Dto;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class HentKravgrunnlagMapperProxy {

    private static final Logger LOG = LoggerFactory.getLogger(HentKravgrunnlagMapperProxy.class);

    private PersonOrganisasjonWrapper pdlAdapterWrapper;

    HentKravgrunnlagMapperProxy() {
        //for CDI proxy
    }

    @Inject
    public HentKravgrunnlagMapperProxy(PersonOrganisasjonWrapper pdlAdapterWrapper) {
        this.pdlAdapterWrapper = pdlAdapterWrapper;
    }

    public Kravgrunnlag431 mapTilDomene(Kravgrunnlag431Dto dto) {
        Kravgrunnlag431 kravgrunnlag431 = formKravgrunnlag431(dto);
        LOG.info("Referanse etter mapping: {}", kravgrunnlag431.getReferanse());
        for (var kravgrunnlagPeriode432Dto : dto.perioder()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = formKravgrunnlagPeriode432(kravgrunnlag431, kravgrunnlagPeriode432Dto);
            for (var postering : kravgrunnlagPeriode432Dto.kravgrunnlagBeloper433()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = formKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                if (!erPosteringenPostitivYtel(kravgrunnlagBelop433)) {
                    kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
                }
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 formKravgrunnlag431(Kravgrunnlag431Dto dto) {
        var gjelderType = GjelderType.fraKode(dto.gjelderType().name());
        var utbetalingGjelderType = GjelderType.fraKode(dto.utbetGjelderType().name());
        return Kravgrunnlag431.builder()
            .medVedtakId(dto.vedtakId())
            .medKravStatusKode(KravStatusKode.fraKode(dto.kravStatusKode().name()))
            .medFagomraadeKode(FagOmrådeKode.fraKode(dto.fagOmrådeKode().name()))
            .medFagSystemId(dto.fagSystemId())
            .medVedtakFagSystemDato(dto.vedtakFagSystemDato())
            .medOmgjortVedtakId(dto.omgjortVedtakId())
            .medGjelderVedtakId(hentAktoerId(gjelderType, dto.gjelderVedtakId()))
            .medGjelderType(gjelderType)
            .medUtbetalesTilId(hentAktoerId(utbetalingGjelderType, dto.utbetalesTilId()))
            .medUtbetIdType(utbetalingGjelderType)
            .medHjemmelKode(dto.hjemmelKode())
            .medBeregnesRenter(dto.beregnesRenter())
            .medAnsvarligEnhet(dto.ansvarligEnhet())
            .medBostedEnhet(dto.bostedEnhet())
            .medBehandlendeEnhet(dto.behandlendeEnhet())
            .medFeltKontroll(dto.kontrollFelt())
            .medSaksBehId(dto.saksBehId())
            .medReferanse(tilHenvisning(dto.referanse()))
            .medEksternKravgrunnlagId(dto.eksternKravgrunnlagId())
            .build();
    }

    private Henvisning tilHenvisning(String referanse) {
        if (referanse == null) {
            return null;
        }
        return new Henvisning(referanse);
    }

    protected String hentAktoerId(GjelderType identType, String ident) {
        return pdlAdapterWrapper.hentAktørIdEllerOrganisajonNummer(ident, identType);
    }

    private KravgrunnlagPeriode432 formKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, KravgrunnlagPeriode432Dto dto) {
        return KravgrunnlagPeriode432.builder()
            .medPeriode(Periode.of(dto.periode().fom(), dto.periode().tom()))
            .medBeløpSkattMnd(dto.beløpSkattMnd())
            .medKravgrunnlag431(kravgrunnlag431)
            .build();
    }

    private KravgrunnlagBelop433 formKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, KravgrunnlagBelop433Dto dto) {
        KlasseType type = map(dto.klasseType());
        return KravgrunnlagBelop433.builder()
            .medKlasseType(type)
            .medKlasseKode(finnKlasseKode(dto.klasseKode(), type))
            .medOpprUtbetBelop(dto.opprUtbetBelop())
            .medNyBelop(dto.nyBelop())
            .medTilbakekrevesBelop(dto.tilbakekrevesBelop())
            .medUinnkrevdBelop(dto.uinnkrevdBelop())
            .medSkattProsent(dto.skattProsent())
            .medResultatKode(dto.resultatKode())
            .medÅrsakKode(dto.årsakKode())
            .medSkyldKode(dto.skyldKode())
            .medKravgrunnlagPeriode432(kravgrunnlagPeriode432)
            .build();
    }

    private static KlasseType map(no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KlasseType typeKlasse) {
        return switch (typeKlasse) {
            case FEIL -> KlasseType.FEIL;
            case JUST -> KlasseType.JUST;
            case SKAT -> KlasseType.SKAT;
            case TREK -> KlasseType.TREK;
            case YTEL -> KlasseType.YTEL;
        };
    }

    private static String finnKlasseKode(String klasseKode, KlasseType klasseType) {
        if (KlasseType.TREK.equals(klasseType) || KlasseType.SKAT.equals(klasseType)) {
            return klasseKode;
        }
        return KlasseKode.fraKode(klasseKode).getKode();
    }

    private boolean erPosteringenPostitivYtel(KravgrunnlagBelop433 belop433) {
        return belop433.getKlasseType().equals(KlasseType.YTEL) && belop433.getNyBelop().compareTo(belop433.getOpprUtbetBelop()) > 0;
    }
}
