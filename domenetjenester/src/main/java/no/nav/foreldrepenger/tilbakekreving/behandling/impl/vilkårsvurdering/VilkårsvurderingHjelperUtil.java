package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatGodTroDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatInfoDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;

public class VilkårsvurderingHjelperUtil {

    VilkårsvurderingHjelperUtil() {
        // for CDI
    }

    static void formGodTroEntitet(VilkårsvurderingPerioderDto periode, VilkårVurderingPeriodeEntitet periodeEntitet) {
        VilkårResultatGodTroDto godTro = (VilkårResultatGodTroDto) periode.getVilkarResultatInfo();
        VilkårVurderingGodTroEntitet godTroEntitet = VilkårVurderingGodTroEntitet.builder().medPeriode(periodeEntitet)
                .medBegrunnelse(godTro.getBegrunnelse())
                .medBeløpErIBehold(godTro.getErBelopetIBehold())
                .medBeløpTilbakekreves(godTro.getTilbakekrevesBelop())
                .build();
        periodeEntitet.setGodTro(godTroEntitet);
    }

    static void formAktsomhetEntitet(VilkårsvurderingPerioderDto periode, VilkårVurderingPeriodeEntitet periodeEntitet) {
        var annetDto = (VilkårResultatAnnetDto) periode.getVilkarResultatInfo();
        var aktsomhet = annetDto.getAktsomhet();
        VilkårVurderingAktsomhetEntitet aktsomhetEntitet;
        if (Aktsomhet.FORSETT.equals(annetDto.getAktsomhet())) {
            var builder = VilkårVurderingAktsomhetEntitet.builder();
            builder.medPeriode(periodeEntitet)
                    .medAktsomhet(aktsomhet)
                    .medBegrunnelse(annetDto.getBegrunnelse());
            if (VilkårResultat.FORSTO_BURDE_FORSTÅTT.equals(periodeEntitet.getVilkårResultat())) {
                builder.medIleggRenter(annetDto.getAktsomhetInfo().isIleggRenter());
            }
            aktsomhetEntitet = builder.build();
        } else {
            var aktsomhetInfo = annetDto.getAktsomhetInfo();
            var tilbakekrevSelvOmBeloepErUnder4Rettsgebyr = aktsomhetInfo.isTilbakekrevSelvOmBeloepErUnder4Rettsgebyr();

            var builder = VilkårVurderingAktsomhetEntitet.builder()
                .medPeriode(periodeEntitet)
                .medAktsomhet(aktsomhet)
                .medBegrunnelse(annetDto.getBegrunnelse())
                .medTilbakekrevSmåBeløp(tilbakekrevSelvOmBeloepErUnder4Rettsgebyr);

            aktsomhetEntitet = builder.build();

            if (tilbakekrevSelvOmBeloepErUnder4Rettsgebyr == null || tilbakekrevSelvOmBeloepErUnder4Rettsgebyr) { // null or true
                var harGrunnerTilReduksjon = aktsomhetInfo.isHarGrunnerTilReduksjon();

                builder.medProsenterSomTilbakekreves(harGrunnerTilReduksjon ? aktsomhetInfo.getAndelTilbakekreves() : null)
                    .medSærligGrunnerTilReduksjon(harGrunnerTilReduksjon)
                    .medIleggRenter(aktsomhetInfo.isIleggRenter())
                    .medBeløpTilbakekreves(aktsomhetInfo.getTilbakekrevesBelop())
                    .medSærligGrunnerBegrunnelse(aktsomhetInfo.getSærligGrunnerBegrunnelse());

                aktsomhetEntitet = builder.build();

                for (var grunn : aktsomhetInfo.getSærligeGrunner()) {
                    var særligGrunnEntitet = VilkårVurderingSærligGrunnEntitet.builder()
                        .medGrunn(grunn)
                        .medVurdertAktsomhet(builder.build())
                        .medBegrunnelse(SærligGrunn.ANNET.equals(grunn) ? aktsomhetInfo.getAnnetBegrunnelse() : null)
                        .build();
                    aktsomhetEntitet.leggTilSærligGrunn(særligGrunnEntitet);
                }
            }
        }
        periodeEntitet.setAktsomhet(aktsomhetEntitet);
    }

    static VilkårResultatInfoDto fylleUtVilkårResultat(VilkårVurderingPeriodeEntitet periodeEntitet) {
        if (periodeEntitet.getGodTro() != null) {
            VilkårVurderingGodTroEntitet godTroEntitet = periodeEntitet.getGodTro();
            return new VilkårResultatGodTroDto(godTroEntitet.getBegrunnelse(), godTroEntitet.isBeløpErIBehold(),
                    godTroEntitet.getBeløpTilbakekreves());
        } else {
            return fylleUtPeiodeForAktsomhet(periodeEntitet);
        }
    }

    static VilkårResultatInfoDto fylleUtPeiodeForAktsomhet(VilkårVurderingPeriodeEntitet periodeEntitet) {
        VilkårVurderingAktsomhetEntitet aktsomhetEntitet = periodeEntitet.getAktsomhet();
        VilkårResultatAktsomhetDto aktsomhetDto = new VilkårResultatAktsomhetDto();
        aktsomhetDto.setIleggRenter(aktsomhetEntitet.getIleggRenter());
        if (!Aktsomhet.FORSETT.equals(aktsomhetEntitet.getAktsomhet())) {
            aktsomhetDto.setTilbakekrevesBelop(aktsomhetEntitet.getManueltTilbakekrevesBeløp());
            aktsomhetDto.setAndelTilbakekreves(aktsomhetEntitet.getProsenterSomTilbakekreves());
            aktsomhetDto.setHarGrunnerTilReduksjon(aktsomhetEntitet.getSærligGrunnerTilReduksjon());
            aktsomhetDto.setTilbakekrevSelvOmBeloepErUnder4Rettsgebyr(aktsomhetEntitet.getTilbakekrevSmåBeløp());
            aktsomhetDto.setSærligGrunnerBegrunnelse(aktsomhetEntitet.getSærligGrunnerBegrunnelse());
            List<SærligGrunn> særligGrunner = new ArrayList<>();
            for (VilkårVurderingSærligGrunnEntitet grunnEntitet : aktsomhetEntitet.getSærligGrunner()) {
                særligGrunner.add(grunnEntitet.getGrunn());
                aktsomhetDto.setAnnetBegrunnelse(grunnEntitet.getBegrunnelse());
            }
            aktsomhetDto.setSærligeGrunner(særligGrunner);
        }
        return new VilkårResultatAnnetDto(aktsomhetEntitet.getBegrunnelse(), aktsomhetEntitet.getAktsomhet(), aktsomhetDto);
    }

    static boolean harEndret(Object forrigeVerdi, Object verdi) {
        return !Objects.equals(forrigeVerdi, verdi);
    }

    static String konvertFraBoolean(Boolean verdi) {
        if (null != verdi) {
            return verdi ? "Ja" : "Nei";
        }
        return null;
    }
}
