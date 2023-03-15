package no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "OppdragIverksettingStatus")
@Table(name = "IVERKSETTING_STATUS_OS")
public class OppdragIverksettingStatusEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IVERKSETTING_STATUS_OS")
    private Long id;

    @Column(name = "behandling_id", updatable = false, nullable = false)
    private Long behandlingId;

    @Column(name = "vedtak_id", updatable = false, nullable = false)
    private String vedtakId;

    @Column(name = "kvittert_tid")
    private LocalDateTime kvittertTidspunkt;

    @Convert(converter = MidlertidigBooleanToStringConverter.class)
    @Column(name = "kvittert_ok")
    private boolean kvitteringOk;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected OppdragIverksettingStatusEntitet() {
    }

    public OppdragIverksettingStatusEntitet(Long behandlingId, String vedtakId) {
        Objects.requireNonNull(behandlingId, "behandlingId");
        Objects.requireNonNull(vedtakId, "vedtakId");
        this.behandlingId = behandlingId;
        this.vedtakId = vedtakId;
    }

    public void registrerKvittering(LocalDateTime kvittertTidspunkt, boolean kvitteringOk) {
        Objects.requireNonNull(kvittertTidspunkt, "kvittertTidspunkt");
        Objects.requireNonNull(kvitteringOk, "kvitteringOk");
        this.kvittertTidspunkt = kvittertTidspunkt;
        this.kvitteringOk = kvitteringOk;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public LocalDateTime getKvittertTidspunkt() {
        return kvittertTidspunkt;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public String getVedtakId() {
        return vedtakId;
    }

    public boolean getKvitteringOk() {
        return kvitteringOk;
    }

    public boolean erSendtOk() {
        return kvitteringOk;
    }
}
