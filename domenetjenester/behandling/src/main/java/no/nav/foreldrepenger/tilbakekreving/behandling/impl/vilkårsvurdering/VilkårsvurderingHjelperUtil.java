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
        VilkårResultatAnnetDto annetDto = (VilkårResultatAnnetDto) periode.getVilkarResultatInfo();
        Aktsomhet aktsomhet = annetDto.getAktsomhet();
        VilkårVurderingAktsomhetEntitet aktsomhetEntitet = null;
        if (Aktsomhet.FORSETT.equals(annetDto.getAktsomhet())) {
            aktsomhetEntitet = VilkårVurderingAktsomhetEntitet.builder().medPeriode(periodeEntitet)
                    .medAktsomhet(aktsomhet)
                    .medBegrunnelse(annetDto.getBegrunnelse()).build();
        } else {
            VilkårResultatAktsomhetDto aktsomhetInfo = annetDto.getAktsomhetInfo();
            aktsomhetEntitet = VilkårVurderingAktsomhetEntitet.builder().medPeriode(periodeEntitet)
                    .medAktsomhet(aktsomhet)
                    .medBegrunnelse(annetDto.getBegrunnelse())
                    .medSærligGrunnerTilReduksjon(aktsomhetInfo.isHarGrunnerTilReduksjon())
                    .medAndelSomTilbakekreves(aktsomhetInfo.getAndelTilbakekreves())
                    .medIleggRenter(aktsomhetInfo.isIleggRenter())
                    .medBeløpTilbakekreves(aktsomhetInfo.getTilbakekrevesBelop())
                    .medTilbakekrevSmåBeløp(aktsomhetInfo.isTilbakekrevSelvOmBeloepErUnder4Rettsgebyr()).build();

            for (SærligGrunn grunn : aktsomhetInfo.getSærligeGrunner()) {
                VilkårVurderingSærligGrunnEntitet særligGrunnEntitet = VilkårVurderingSærligGrunnEntitet.builder()
                        .medGrunn(grunn)
                        .medVurdertAktsomhet(aktsomhetEntitet)
                        .medBegrunnelse(SærligGrunn.ANNET.equals(grunn) ? aktsomhetInfo.getAnnetBegrunnelse() : null).build();
                aktsomhetEntitet.leggTilSærligGrunn(særligGrunnEntitet);
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
        if (!Aktsomhet.FORSETT.equals(aktsomhetEntitet.getAktsomhet())) {
            VilkårResultatAktsomhetDto aktsomhetDto = new VilkårResultatAktsomhetDto();
            aktsomhetDto.setTilbakekrevesBelop(aktsomhetEntitet.getManueltTilbakekrevesBeløp());
            aktsomhetDto.setAndelTilbakekreves(aktsomhetEntitet.getAndelSomTilbakekreves());
            aktsomhetDto.setIleggRenter(aktsomhetEntitet.getIleggRenter());
            aktsomhetDto.setHarGrunnerTilReduksjon(aktsomhetEntitet.getSærligGrunnerTilReduksjon());
            aktsomhetDto.setTilbakekrevSelvOmBeloepErUnder4Rettsgebyr(aktsomhetEntitet.getTilbakekrevSmåBeløp());
            List<SærligGrunn> særligGrunner = new ArrayList<>();
            for (VilkårVurderingSærligGrunnEntitet grunnEntitet : aktsomhetEntitet.getSærligGrunner()) {
                særligGrunner.add(grunnEntitet.getGrunn());
                aktsomhetDto.setAnnetBegrunnelse(grunnEntitet.getBegrunnelse());
            }
            aktsomhetDto.setSærligeGrunner(særligGrunner);
            return new VilkårResultatAnnetDto(aktsomhetEntitet.getBegrunnelse(), aktsomhetEntitet.getAktsomhet(), aktsomhetDto);
        }
        return new VilkårResultatAnnetDto(aktsomhetEntitet.getBegrunnelse(), aktsomhetEntitet.getAktsomhet(), null);
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
