package no.nav.foreldrepenger.tilbakekreving.historikk.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public class HistorikkinnslagDelDto {
    @Deprecated // Skriv om K9-frontend til å vente seg begrunnelsetekst
    private Kodeverdi begrunnelse;
    private String begrunnelsetekst;
    private String begrunnelseFritekst;
    private HistorikkinnslagHendelseDto hendelse;
    private List<HistorikkinnslagOpplysningDto> opplysninger;
    private SkjermlenkeType skjermlenke;
    @Deprecated // Skriv om K9-frontend til å vente seg årsaktekst
    private Kodeverdi aarsak;
    private String årsaktekst;
    private HistorikkInnslagTemaDto tema;
    private HistorikkInnslagGjeldendeFraDto gjeldendeFra;
    private String resultat;
    private List<HistorikkinnslagEndretFeltDto> endredeFelter;
    private List<HistorikkinnslagTotrinnsVurderingDto> aksjonspunkter;

    static List<HistorikkinnslagDelDto> mapFra(List<HistorikkinnslagDel> historikkinnslagDelList) {
        List<HistorikkinnslagDelDto> historikkinnslagDelDtoList = new ArrayList<>();
        for (HistorikkinnslagDel historikkinnslagDel : historikkinnslagDelList) {
            historikkinnslagDelDtoList.add(mapFra(historikkinnslagDel));
        }
        return historikkinnslagDelDtoList;
    }

    private static HistorikkinnslagDelDto mapFra(HistorikkinnslagDel historikkinnslagDel) {
        HistorikkinnslagDelDto dto = new HistorikkinnslagDelDto();
        var begrunnelseKodeverdi = historikkinnslagDel.getBegrunnelseFelt().flatMap(HistorikkinnslagDelDto::finnÅrsakKodeListe);
        dto.setBegrunnelse(begrunnelseKodeverdi.orElse(null)); // Fjernes når K9 slutter å bruke den
        if (begrunnelseKodeverdi.isEmpty()) {
            historikkinnslagDel.getBegrunnelse().ifPresent(dto::setBegrunnelseFritekst);
        } else {
            dto.setBegrunnelsetekst(begrunnelseKodeverdi.get().getNavn());
        }
        historikkinnslagDel.getAarsakFelt()
            .flatMap(HistorikkinnslagDelDto::finnÅrsakKodeListe)
            .ifPresent(årsak -> {
                dto.setAarsak(årsak);  // Fjernes når K9 slutter å bruke den
                dto.setÅrsaktekst(årsak.getNavn());
            });
        historikkinnslagDel.getTema().ifPresent(felt -> dto.setTema(HistorikkInnslagTemaDto.mapFra(felt)));
        historikkinnslagDel.getGjeldendeFraFelt().ifPresent(felt -> {
            if (felt.getNavn() != null && felt.getNavnVerdi() != null && felt.getTilVerdi() != null) {
                dto.setGjeldendeFra(felt.getTilVerdi(), felt.getNavn(), felt.getNavnVerdi());
            } else if (felt.getTilVerdi() != null) {
                dto.setGjeldendeFra(felt.getTilVerdi());
            }
        });
        historikkinnslagDel.getResultat().ifPresent(dto::setResultat);
        historikkinnslagDel.getHendelse().ifPresent(hendelse -> {
            HistorikkinnslagHendelseDto hendelseDto = HistorikkinnslagHendelseDto.mapFra(hendelse);
            dto.setHendelse(hendelseDto);
        });
        historikkinnslagDel.getSkjermlenke().ifPresent(skjermlenke -> {
            SkjermlenkeType type = SkjermlenkeType.fraKode(skjermlenke);
            dto.setSkjermlenke(type);
        });
        if (!historikkinnslagDel.getTotrinnsvurderinger().isEmpty()) {
            dto.setAksjonspunkter(HistorikkinnslagTotrinnsVurderingDto.mapFra(historikkinnslagDel.getTotrinnsvurderinger()));
        }
        if (!historikkinnslagDel.getOpplysninger().isEmpty()) {
            dto.setOpplysninger(HistorikkinnslagOpplysningDto.mapFra(historikkinnslagDel.getOpplysninger()));
        }
        if (!historikkinnslagDel.getEndredeFelt().isEmpty()) {
            dto.setEndredeFelter(HistorikkinnslagEndretFeltDto.mapFra(historikkinnslagDel.getEndredeFelt()));
        }
        return dto;
    }

    private static Optional<Kodeverdi> finnÅrsakKodeListe(HistorikkinnslagFelt aarsak) {
        String aarsakVerdi = aarsak.getTilVerdi();
        if (Objects.equals("-", aarsakVerdi)) {
            return Optional.empty();
        }
        if (aarsak.getKlTilVerdi() == null) {
            return Optional.empty();
        }

        var kodeverdiMap = HistorikkInnslagTekstBuilder.KODEVERK_KODEVERDI_MAP.get(aarsak.getKlTilVerdi());
        if (kodeverdiMap == null) {
            throw new IllegalStateException("Har ikke støtte for HistorikkinnslagFelt#klTilVerdi=" + aarsak.getKlTilVerdi());
        }
        return Optional.ofNullable(kodeverdiMap.get(aarsakVerdi));
    }

    public Kodeverdi getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(Kodeverdi begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public String getBegrunnelseFritekst() {
        return begrunnelseFritekst;
    }

    public void setBegrunnelseFritekst(String begrunnelseFritekst) {
        this.begrunnelseFritekst = begrunnelseFritekst;
    }

    public String getBegrunnelsetekst() {
        return begrunnelsetekst;
    }

    public void setBegrunnelsetekst(String begrunnelsetekst) {
        this.begrunnelsetekst = begrunnelsetekst;
    }

    public HistorikkinnslagHendelseDto getHendelse() {
        return hendelse;
    }

    public void setHendelse(HistorikkinnslagHendelseDto hendelse) {
        this.hendelse = hendelse;
    }

    public SkjermlenkeType getSkjermlenke() {
        return skjermlenke;
    }

    public void setSkjermlenke(SkjermlenkeType skjermlenke) {
        this.skjermlenke = skjermlenke;
    }

    public Kodeverdi getAarsak() {
        return aarsak;
    }

    public void setAarsak(Kodeverdi aarsak) {
        this.aarsak = aarsak;
    }

    public String getÅrsaktekst() {
        return årsaktekst;
    }

    public void setÅrsaktekst(String årsaktekst) {
        this.årsaktekst = årsaktekst;
    }

    public HistorikkInnslagTemaDto getTema() {
        return tema;
    }

    public void setTema(HistorikkInnslagTemaDto tema) {
        this.tema = tema;
    }

    public HistorikkInnslagGjeldendeFraDto getGjeldendeFra() {
        return gjeldendeFra;
    }

    public void setGjeldendeFra(String fra) {
        if (this.gjeldendeFra == null) {
            this.gjeldendeFra = new HistorikkInnslagGjeldendeFraDto(fra);
        } else {
            this.gjeldendeFra.setFra(fra);
        }
    }

    public void setGjeldendeFra(String fra, String navn, String verdi) {
        if (this.gjeldendeFra == null) {
            this.gjeldendeFra = new HistorikkInnslagGjeldendeFraDto(fra, navn, verdi);
        } else {
            this.gjeldendeFra.setFra(fra);
            this.gjeldendeFra.setNavn(navn);
            this.gjeldendeFra.setVerdi(verdi);
        }
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public List<HistorikkinnslagEndretFeltDto> getEndredeFelter() {
        return endredeFelter;
    }

    public void setEndredeFelter(List<HistorikkinnslagEndretFeltDto> endredeFelter) {
        this.endredeFelter = endredeFelter;
    }

    public List<HistorikkinnslagOpplysningDto> getOpplysninger() {
        return opplysninger;
    }

    public void setOpplysninger(List<HistorikkinnslagOpplysningDto> opplysninger) {
        this.opplysninger = opplysninger;
    }

    public List<HistorikkinnslagTotrinnsVurderingDto> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public void setAksjonspunkter(List<HistorikkinnslagTotrinnsVurderingDto> aksjonspunkter) {
        this.aksjonspunkter = aksjonspunkter;
    }

}
