package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.NavOppfulgt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.ObjectMapperFactory;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.opentest4j.AssertionFailedError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;

public class KodeverdiDeSerialiseringTest {

    private final ObjectMapper overstyrSomObjektMapper = ObjectMapperFactory.getDefaultObjectMapperCopy(true);
    private final ObjectMapper overstyrSomStringMapper = ObjectMapperFactory.getDefaultObjectMapperCopy(false);
    private final ObjectMapper openapiMapper = ObjectMapperFactory.getOpenapiObjectMapper();

    private boolean isDefaultObjectSerialized(final Kodeverdi kv) {
        return true;
    }

    private <KV extends Kodeverdi> void checkObjektObjectMapperKodeverdi(KV kv) throws JsonProcessingException {
        final String serialisert = overstyrSomObjektMapper.writeValueAsString(kv);
        final Class<?> cls = kv.getClass().isAnonymousClass() ? kv.getClass().getSuperclass() : kv.getClass();
        if(isDefaultObjectSerialized(kv)) {
            final String kodeStr = kv.getKode() != null ? "\"" + kv.getKode() + "\"" : "null";
            assertThat(serialisert)
                .as("kodeverk: %s, klasse: %s objekt start {", kv.getKodeverk(), cls.getSimpleName())
                .startsWith("{");
            assertThat(serialisert)
                .as("kodeverk: %s, klasse: %s kode ikkje lik", kv.getKodeverk(), cls.getSimpleName())
                .containsIgnoringWhitespaces("\"kode\": " + kodeStr);
            assertThat(serialisert).endsWith("}");
        } else {
            assertThat(serialisert).isEqualToIgnoringWhitespace("\"" + kv.getKode() + "\"");
        }
        assertThat(Kodeverdi.class.isAssignableFrom(cls)).isTrue();
        final Kodeverdi deserialisert = overstyrSomObjektMapper.readValue(serialisert, (Class<? extends Kodeverdi>) cls);
        assertThat(deserialisert)
            .as("kodeverk: %s, klasse: %s, deserialisert frå objekt (%s) er not null", kv.getKodeverk(), cls.getSimpleName(), serialisert)
            .isNotNull();
        assertThat(deserialisert)
            .as("kodeverk: %s, klasse: %s, deserialisert frå objekt (%s) er korrekt klasse", kv.getKodeverk(), cls.getSimpleName(), serialisert)
            .isInstanceOf(kv.getClass());
        assertThat(deserialisert).isEqualTo(kv);
        assertThat(deserialisert.getClass().getSimpleName()).isEqualTo(kv.getClass().getSimpleName());
    }

    private <KV extends Kodeverdi> void checkStringObjectMapperKodeverdi(KV kv) throws JsonProcessingException {
        final String serialisert = overstyrSomStringMapper.writeValueAsString(kv);
        final String kodeStr = kv.getKode() != null ? "\"" + kv.getKode() + "\"" : "null";
        assertThat(serialisert).isEqualToIgnoringWhitespace(kodeStr);
        final Class<?> cls = kv.getClass().isAnonymousClass() ? kv.getClass().getSuperclass() : kv.getClass();
        assertThat(Kodeverdi.class.isAssignableFrom(cls)).isTrue();
        final Kodeverdi deserialisert = overstyrSomStringMapper.readValue(serialisert, (Class<? extends Kodeverdi>) cls);
        if(deserialisert != null) { // Nokon Kodeverdi typer har UNDEFINED som serialisert til string blir null
            assertThat(deserialisert)
                .as("kodeverk: %s, klasse: %s, deserialisert frå string er korrekt klasse", kv.getKodeverk(), cls.getSimpleName())
                .isInstanceOf(kv.getClass());
            assertThat(deserialisert).isEqualTo(kv);
            assertThat(deserialisert.getClass().getSimpleName()).isEqualTo(kv.getClass().getSimpleName());
        }
    }

    private <KV extends Kodeverdi> void checkOpenapiObjectMapperKodeverdi(KV kv) throws JsonProcessingException {
        final String serialisert = openapiMapper.writeValueAsString(kv);
        // Nokon enum instanser er anonyme subklasser av enum klassen, må bruke getEnclosingClass for å finne info om
        // enum klassen då.
        final boolean isAnon = kv.getClass().isAnonymousClass();
        final Class<?> cls = isAnon ? kv.getClass().getEnclosingClass() : kv.getClass();
        final boolean isEnum = cls.isEnum();
        if(isEnum) {
            try {
                final String expected = kv.toString() != null ? kv.toString() : "";
                assertThat(serialisert)
                    .as("kodeverk: %s, klasse: %s toString ikkje lik", kv.getKodeverk(), cls.getSimpleName())
                    .isEqualToIgnoringWhitespace("\"" + expected + "\"");
            } catch (AssertionFailedError e) {
                final String expected = kv.getKode() != null ? kv.getKode() : "";
                assertThat(serialisert)
                    .as("kodeverk: %s, klasse: %s toString ikkje lik, getKode ikkje lik", kv.getKodeverk(), cls.getSimpleName())
                    .isEqualToIgnoringWhitespace("\"" + expected + "\"");
            }
        } // Else is default serialization, no need, and hard to check generally here

        assertThat(Kodeverdi.class.isAssignableFrom(cls)).isTrue();
        final Kodeverdi deserialisert = openapiMapper.readValue(serialisert, (Class<? extends Kodeverdi>) cls);
        assertThat(deserialisert).isInstanceOf(kv.getClass());
        assertThat(deserialisert).isEqualTo(kv);
        assertThat(deserialisert.getClass().getSimpleName()).isEqualTo(kv.getClass().getSimpleName());
    }

    static Set<EnumSet<? extends Kodeverdi>> alleKodeverdier = Set.of(
        EnumSet.allOf(FagsakYtelseType.class),
        EnumSet.allOf(BehandlingResultatType.class),
        EnumSet.allOf(BehandlingStatus.class),
        EnumSet.allOf(BehandlingStegStatus.class),
        EnumSet.allOf(BehandlingStegType.class),
        EnumSet.allOf(BehandlingType.class),
        EnumSet.allOf(BehandlingÅrsakType.class),
        EnumSet.allOf(ForeldelseVurderingType.class),
        EnumSet.allOf(KlasseKode.class),
        EnumSet.allOf(AksjonspunktDefinisjon.class),
        EnumSet.allOf(AksjonspunktStatus.class),
        EnumSet.allOf(AksjonspunktType.class),
        EnumSet.allOf(Venteårsak.class),
        EnumSet.allOf(VurderÅrsak.class),
        EnumSet.allOf(BrevType.class),
        EnumSet.allOf(VedtaksbrevFritekstType.class),
        EnumSet.allOf(SivilstandType.class),
        EnumSet.allOf(SkjermlenkeType.class),
        EnumSet.allOf(VergeType.class),
        EnumSet.allOf(DokumentMalType.class),
        EnumSet.allOf(FagOmrådeKode.class),
        EnumSet.allOf(Fagsystem.class),
        EnumSet.allOf(HendelseType.class),
        EnumSet.allOf(HendelseUnderType.class),
        EnumSet.allOf(Landkoder.class),
        EnumSet.allOf(Språkkode.class),
        EnumSet.allOf(HistorikkAktør.class),
        EnumSet.allOf(VidereBehandling.class),
        EnumSet.allOf(IverksettingStatus.class),
        EnumSet.allOf(VedtakResultatType.class),
        EnumSet.allOf(Aktsomhet.class),
        EnumSet.allOf(AnnenVurdering.class),
        EnumSet.allOf(Inntektskategori.class),
        EnumSet.allOf(NavOppfulgt.class),
        EnumSet.allOf(SærligGrunn.class),
        EnumSet.allOf(VilkårResultat.class),
        EnumSet.allOf(GjelderType.class),
        EnumSet.allOf(KlasseType.class),
        EnumSet.allOf(KravStatusKode.class),
        EnumSet.allOf(MeldingType.class)
    );

    @ParameterizedTest
    @FieldSource("alleKodeverdier")
    public void testAlleKodeverdier(final EnumSet<? extends Kodeverdi> kodeverdier) throws JsonProcessingException {
        for(final var v: kodeverdier) {
            checkObjektObjectMapperKodeverdi(v);
            checkStringObjectMapperKodeverdi(v);
            checkObjektObjectMapperKodeverdi(v);
        }
    }
}
