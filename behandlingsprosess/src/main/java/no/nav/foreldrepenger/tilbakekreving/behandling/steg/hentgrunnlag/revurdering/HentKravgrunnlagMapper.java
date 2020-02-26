package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TpsAdapterWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;

@ApplicationScoped
public class HentKravgrunnlagMapper {
    private Logger logger = LoggerFactory.getLogger(HentKravgrunnlagMapper.class);
    private TpsAdapterWrapper tpsAdapterWrapper;

    HentKravgrunnlagMapper() {
        //for CDI proxy
    }

    @Inject
    public HentKravgrunnlagMapper(TpsAdapterWrapper tpsAdapterWrapper) {
        this.tpsAdapterWrapper = tpsAdapterWrapper;
    }

    public Kravgrunnlag431 mapTilDomene(DetaljertKravgrunnlagDto dto) {
        Kravgrunnlag431 kravgrunnlag431 = formKravgrunnlag431(dto);
        for (DetaljertKravgrunnlagPeriodeDto periodeDto : dto.getTilbakekrevingsPeriode()) {
            KravgrunnlagPeriode432 kravgrunnlagPeriode432 = formKravgrunnlagPeriode432(kravgrunnlag431, periodeDto);
            for (DetaljertKravgrunnlagBelopDto postering : periodeDto.getTilbakekrevingsBelop()) {
                KravgrunnlagBelop433 kravgrunnlagBelop433 = formKravgrunnlagBelop433(kravgrunnlagPeriode432, postering);
                kravgrunnlagPeriode432.leggTilBeløp(kravgrunnlagBelop433);
            }
            kravgrunnlag431.leggTilPeriode(kravgrunnlagPeriode432);
        }
        return kravgrunnlag431;
    }

    private Kravgrunnlag431 formKravgrunnlag431(DetaljertKravgrunnlagDto dto) {
        GjelderType gjelderType = GjelderType.fraKode(dto.getTypeGjelderId().value());
        GjelderType utbetalingGjelderType = GjelderType.fraKode(dto.getTypeUtbetId().value());
        return Kravgrunnlag431.builder().medVedtakId(dto.getVedtakId().longValue())
            .medKravStatusKode(KravStatusKode.fraKode(trimTrailingSpaces(dto.getKodeStatusKrav())))
            .medFagomraadeKode(FagOmrådeKode.fraKode(trimTrailingSpaces(dto.getKodeFagomraade().trim())))
            .medFagSystemId(trimTrailingSpaces(dto.getFagsystemId()))
            .medVedtakFagSystemDato(konverter(dto.getDatoVedtakFagsystem()))
            .medOmgjortVedtakId(dto.getVedtakIdOmgjort() != null ? dto.getVedtakIdOmgjort().longValue() : null)
            .medGjelderVedtakId(hentAktoerId(gjelderType, dto.getVedtakGjelderId()))
            .medGjelderType(gjelderType)
            .medUtbetalesTilId(hentAktoerId(utbetalingGjelderType, dto.getUtbetalesTilId()))
            .medUtbetIdType(utbetalingGjelderType)
            .medHjemmelKode(dto.getKodeHjemmel())
            .medBeregnesRenter(dto.getRenterBeregnes() != null ? dto.getRenterBeregnes().value() : null)
            .medAnsvarligEnhet(trimTrailingSpaces(dto.getEnhetAnsvarlig()))
            .medBostedEnhet(trimTrailingSpaces(dto.getEnhetBosted()))
            .medBehandlendeEnhet(trimTrailingSpaces(dto.getEnhetBehandl()))
            .medFeltKontroll(dto.getKontrollfelt())
            .medSaksBehId(trimTrailingSpaces(dto.getSaksbehId()))
            .medReferanse(dto.getReferanse())
            .medEksternKravgrunnlagId(String.valueOf(dto.getKravgrunnlagId()))
            .build();
    }

    private String hentAktoerId(GjelderType identType, String ident) {
        if (isInDevMode()) {
            logger.warn("Hentet kravgrunnlag i utviklermodus. Skal ikke skje i testmiljø eller produksjon, det er for lokalt utviklingsmiljø. Lagrer mocket aktørId i databasen.");
            return "mock" + ident;
        }
        return tpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(ident, identType);
    }

    private boolean isInDevMode() {
        Optional<String> environmentName = EnvironmentProperty.getEnvironmentName();
        return environmentName.isPresent() && environmentName.get().equalsIgnoreCase("devimg");
    }

    private KravgrunnlagPeriode432 formKravgrunnlagPeriode432(Kravgrunnlag431 kravgrunnlag431, DetaljertKravgrunnlagPeriodeDto dto) {
        LocalDate fom = konverter(dto.getPeriode().getFom());
        LocalDate tom = konverter(dto.getPeriode().getTom());
        return KravgrunnlagPeriode432.builder()
            .medPeriode(Periode.of(fom, tom))
            .medBeløpSkattMnd(dto.getBelopSkattMnd())
            .medKravgrunnlag431(kravgrunnlag431)
            .build();
    }

    private KravgrunnlagBelop433 formKravgrunnlagBelop433(KravgrunnlagPeriode432 kravgrunnlagPeriode432, DetaljertKravgrunnlagBelopDto dto) {
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

    private KlasseType map(TypeKlasseDto typeKlasse) {
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

    private String finnKlasseKode(String klasseKode, KlasseType klasseType){
        if(KlasseType.TREK.equals(klasseType) || KlasseType.SKAT.equals(klasseType)){
            return klasseKode;
        }
        return KlasseKode.fraKode(klasseKode).getKode();
    }

    private String trimTrailingSpaces(String field){
        return field.trim();
    }

}
