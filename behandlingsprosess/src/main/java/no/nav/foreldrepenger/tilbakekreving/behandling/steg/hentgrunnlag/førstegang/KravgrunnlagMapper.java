package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

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
import no.nav.foreldrepenger.xmlutils.DateUtil;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelop;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriode;
import no.nav.tilbakekreving.typer.v1.TypeKlasse;

@ApplicationScoped
public class KravgrunnlagMapper {

    private PersonOrganisasjonWrapper tpsAdapterWrapper;

    KravgrunnlagMapper() {
        //for CDI proxy
    }

    @Inject
    public KravgrunnlagMapper(PersonOrganisasjonWrapper tpsAdapterWrapper) {
        this.tpsAdapterWrapper = tpsAdapterWrapper;
    }

    public Henvisning finnHenvisning(DetaljertKravgrunnlag kravgrunnlagDto) {
        return new Henvisning(kravgrunnlagDto.getReferanse());
    }

    public Kravgrunnlag431 mapTilDomene(DetaljertKravgrunnlag dto) {
        Kravgrunnlag431 kravgrunnlag431 = lagKravgrunnlag431(dto);
        for (DetaljertKravgrunnlagPeriode periodeDto : dto.getTilbakekrevingsPeriode()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = lagKravgrunnlagPeriode432(kravgrunnlag431, periodeDto);
            for (DetaljertKravgrunnlagBelop postering : periodeDto.getTilbakekrevingsBelop()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = lagKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                if (!erPosteringenPostitivYtel(kravgrunnlagBelop433)) {
                    kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
                }
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 lagKravgrunnlag431(DetaljertKravgrunnlag dto) {
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
                .medReferanse(new Henvisning(dto.getReferanse()))
                .build();
    }

    private KravgrunnlagPeriode432 lagKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, DetaljertKravgrunnlagPeriode dto) {
        LocalDate fom = konverter(dto.getPeriode().getFom());
        LocalDate tom = konverter(dto.getPeriode().getTom());
        return KravgrunnlagPeriode432.builder()
                .medPeriode(Periode.of(fom, tom))
                .medBeløpSkattMnd(dto.getBelopSkattMnd())
                .medKravgrunnlag431(kravgrunnlag431)
                .build();
    }

    private KravgrunnlagBelop433 lagKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, DetaljertKravgrunnlagBelop dto) {
        KlasseType type = map(dto.getTypeKlasse());
        return KravgrunnlagBelop433.builder()
                .medKlasseType(type)
                .medKlasseKode(finnKlasseKode(dto.getKodeKlasse(), type))
                .medOpprUtbetBelop(dto.getBelopOpprUtbet())
                .medNyBelop(dto.getBelopNy())
                .medTilbakekrevesBelop(dto.getBelopTilbakekreves())
                .medUinnkrevdBelop(dto.getBelopUinnkrevd())
                .medSkattProsent(dto.getSkattProsent())
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
        return switch (typeKlasse) {
            case FEIL -> KlasseType.FEIL;
            case JUST -> KlasseType.JUST;
            case SKAT -> KlasseType.SKAT;
            case TREK -> KlasseType.TREK;
            case YTEL -> KlasseType.YTEL;
            default -> throw new IllegalArgumentException("Ukjent klassetype: " + typeKlasse);
        };
    }

    private String finnKlasseKode(String klasseKode, KlasseType klasseType) {
        if (KlasseType.TREK.equals(klasseType) || KlasseType.SKAT.equals(klasseType)) {
            return klasseKode;
        }
        return KlasseKode.fraKode(klasseKode).getKode();
    }

    private boolean erPosteringenPostitivYtel(KravgrunnlagBelop433 belop433) {
        return belop433.getKlasseType().equals(KlasseType.YTEL) && belop433.getNyBelop().compareTo(belop433.getOpprUtbetBelop()) > 0;
    }

}
