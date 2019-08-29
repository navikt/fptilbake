package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TpsAdapterWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelop;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriode;
import no.nav.tilbakekreving.typer.v1.TypeKlasse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@ApplicationScoped
public class KravgrunnlagMapper {

    private TpsAdapterWrapper tpsAdapterWrapper;

    KravgrunnlagMapper() {
        //for CDI proxy
    }

    @Inject
    public KravgrunnlagMapper(TpsAdapterWrapper tpsAdapterWrapper) {
        this.tpsAdapterWrapper = tpsAdapterWrapper;
    }

    public String finnBehandlngId(DetaljertKravgrunnlag kravgrunnlagDto) {
        return kravgrunnlagDto.getReferanse();
    }

    public String finnSaksnummer(DetaljertKravgrunnlag kravgrunnlagDto) {
        return kravgrunnlagDto.getFagsystemId().substring(0,kravgrunnlagDto.getFagsystemId().length()-3);
    }

    public Kravgrunnlag431 mapTilDomene(DetaljertKravgrunnlag dto) {
        Kravgrunnlag431 kravgrunnlag431 = formKravgrunnlag431(dto);
        for (DetaljertKravgrunnlagPeriode periodeDto : dto.getTilbakekrevingsPeriode()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = formKravgrunnlagPeriode432(kravgrunnlag431, periodeDto);
            for (DetaljertKravgrunnlagBelop postering : periodeDto.getTilbakekrevingsBelop()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = formKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 formKravgrunnlag431(DetaljertKravgrunnlag dto) {
        GjelderType gjelderType = GjelderType.fraKode(dto.getTypeGjelderId().value());
        GjelderType utbetalingGjelderType = GjelderType.fraKode(dto.getTypeUtbetId().value());
        return Kravgrunnlag431.builder()
            .medVedtakId(dto.getVedtakId().longValue())
            .medEksternKravgrunnlagId(dto.getKravgrunnlagId().toString())
            .medKravStatusKode(KravStatusKode.fraKode(dto.getKodeStatusKrav()))
            .medFagomraadeKode(FagOmrådeKode.fraKode(dto.getKodeFagomraade()))
            .medFagSystemId(dto.getFagsystemId())
            .medVedtakFagSystemDato(konverter(dto.getDatoVedtakFagsystem()))
            .medOmgjortVedtakId(dto.getVedtakIdOmgjort() != null ? dto.getVedtakIdOmgjort().longValue() : null)
            .medGjelderVedtakId(tpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(dto.getVedtakGjelderId(), gjelderType))
            .medGjelderType(gjelderType)
            .medUtbetalesTilId(tpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(dto.getUtbetalesTilId(), utbetalingGjelderType))
            .medUtbetIdType(utbetalingGjelderType)
            .medHjemmelKode(dto.getKodeHjemmel())
            .medBeregnesRenter(dto.getRenterBeregnes() != null ? dto.getRenterBeregnes().value() : null)
            .medAnsvarligEnhet(dto.getEnhetAnsvarlig())
            .medBostedEnhet(dto.getEnhetBosted())
            .medBehandlendeEnhet(dto.getEnhetBehandl())
            .medFeltKontroll(dto.getKontrollfelt())
            .medSaksBehId(dto.getSaksbehId())
            .medReferanse(dto.getReferanse())
            .build();
    }

    private KravgrunnlagPeriode432 formKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, DetaljertKravgrunnlagPeriode dto) {
        LocalDate fom = konverter(dto.getPeriode().getFom());
        LocalDate tom = konverter(dto.getPeriode().getTom());
        return KravgrunnlagPeriode432.builder()
            .medPeriode(Periode.of(fom, tom))
            .medKravgrunnlag431(kravgrunnlag431)
            .build();
    }

    private KravgrunnlagBelop433 formKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, DetaljertKravgrunnlagBelop dto) {
        KlasseType type = map(dto.getTypeKlasse());
        return KravgrunnlagBelop433.builder()
            .medKlasseType(type)
            .medKlasseKode(dto.getKodeKlasse())
            .medOpprUtbetBelop(dto.getBelopOpprUtbet())
            .medNyBelop(dto.getBelopNy())
            .medTilbakekrevesBelop(dto.getBelopTilbakekreves())
            .medUinnkrevdBelop(dto.getBelopUinnkrevd())
            .medResultatKode(dto.getKodeResultat())
            .medÅrsakKode(dto.getKodeAArsak())
            .medSkyldKode(dto.getKodeSkyld())
            .medKravgrunnlagPeriode432(kravgrunnlagPeriode432)
            .build();
    }

    private static LocalDate konverter(XMLGregorianCalendar dato) {
        return DateUtil.convertToLocalDate(dato);
    }

    private KlasseType map(TypeKlasse typeKlasse) {
        switch (typeKlasse) {
            case FEIL:
                return KlasseType.FEIL;
            case JUST:
                return KlasseType.JUST;
            case SKAT:
                return KlasseType.SKAT;
            case TREK:
                return KlasseType.TREK;
            case YTEL:
                return KlasseType.YTEL;
            default:
                throw new IllegalArgumentException("Ukjent klassetype: " + typeKlasse);
        }
    }

}
