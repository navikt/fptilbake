package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.exception.IntegrasjonException;

public class ØkonomiConsumerFeil {

    public static IntegrasjonException fikkFeilkodeVedHentingAvKravgrunnlag(Long behandlingId, String infoFraKvittering) {
        return new IntegrasjonException("FPT-539078", String.format("Fikk feil fra OS ved henting av kravgrunnlag for behandlingId=%s.%s", behandlingId, infoFraKvittering));
    }

    public static IntegrasjonException fikkFeilkodeVedAnnulereKravgrunnlag(Long behandlingId, String infoFraKvittering) {
        return new IntegrasjonException("FPT-539079", String.format("Fikk feil fra OS ved annulere kravgrunnlag for behandlingId=%s.%s", behandlingId, infoFraKvittering));
    }

    public static IntegrasjonException fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagIkkeFinnes(Long behandlingId, Long kravgrunnlagId, String infoFraKvittering) {
        return new ManglendeKravgrunnlagException("FPT-539080", String.format("Fikk feil fra OS ved henting av kravgrunnlag for behandlingId=%s og kravgrunnlagId=%s.%s", behandlingId, kravgrunnlagId, infoFraKvittering));
    }

    public static IntegrasjonException fikkFeilkodeVedHentingAvKravgrunnlagNårKravgrunnlagErSperret(Long behandlingId, Long kravgrunnlagId, String infoFraKvittering) {
        return new SperringKravgrunnlagException("FPT-539081", String.format("Fikk feil fra OS ved henting av kravgrunnlag for behandlingId=%s og kravgrunnlagId=%s.%s", behandlingId, kravgrunnlagId, infoFraKvittering));
    }

    public static IntegrasjonException fikkUkjentFeilkodeVedHentingAvKravgrunnlag(Long behandlingId, Long kravgrunnlagId, String infoFraKvittering) {
        return new UkjentOppdragssystemException("FPT-539085", String.format("Fikk ukjent feil fra OS ved henting av kravgrunnlag for behandlingId=%s og kravgrunnlagId=%s.%s", behandlingId, kravgrunnlagId, infoFraKvittering));
    }

    public static String formaterKvittering(MmelDto kvittering) {
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
