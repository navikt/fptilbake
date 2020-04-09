package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface ØkonomiConsumerFeil extends DeklarerteFeil {
    ØkonomiConsumerFeil FACTORY = FeilFactory.create(ØkonomiConsumerFeil.class);

    @IntegrasjonFeil(feilkode = "FPT-539078", feilmelding = "Fikk feil fra OS ved henting av kravgrunnlag for behandlingId=%s.%s", logLevel = LogLevel.WARN)
    Feil fikkFeilkodeVedHentingAvKravgrunnlag(Long behandlingId, String infoFraKvittering);

    @IntegrasjonFeil(feilkode = "FPT-539079", feilmelding = "Fikk feil fra OS ved annulere kravgrunnlag for behandlingId=%s.%s", logLevel = LogLevel.WARN)
    Feil fikkFeilkodeVedAnnulereKravgrunnlag(Long behandlingId, String infoFraKvittering);

    @IntegrasjonFeil(feilkode = "FPT-539080", feilmelding = "Fikk feil fra OS ved henting av kravgrunnlag for behandlingId=%s.%s", logLevel = LogLevel.WARN)
    Feil fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagIkkeFinnes(Long behandlingId, String infoFraKvittering);

    static String formaterKvittering(MmelDto kvittering) {
        //HAXX ikke bruk dette som mal ved oppsett av deklarative feil
        //.... brukes her siden det er veldig mange parametre som skal logges
        //.... reduserer sjangsen for at parametre stokkes feil ved fremtidig endring
        StringBuilder builder = new StringBuilder();
        addToBuilder(builder, "Alvorlighetsgrad", kvittering.getAlvorlighetsgrad());
        addToBuilder(builder, "KodeMelding", kvittering.getKodeMelding());
        addToBuilder(builder, "ProgramId", kvittering.getProgramId());
        addToBuilder(builder, "SectionNavn", kvittering.getSectionNavn());
        addToBuilder(builder, "SqlKode", kvittering.getSqlKode());
        addToBuilder(builder, "SqlMelding", kvittering.getSqlMelding());
        addToBuilder(builder, "SqlState", kvittering.getSqlState());
        addToBuilder(builder, "SystemId", kvittering.getSystemId());
        addToBuilder(builder, "MqCompletionKode", kvittering.getMqCompletionKode());
        addToBuilder(builder, "MqReasonKode", kvittering.getMqReasonKode());
        addToBuilder(builder, "BeskrMelding", kvittering.getBeskrMelding());
        return builder.toString();
    }

    private static void addToBuilder(StringBuilder builder, String name, String value) {
        if (value != null) {
            builder.append(" ")
                    .append(name)
                    .append("='")
                    .append(value)
                    .append("'");
        }
    }

}
