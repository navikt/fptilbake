package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;


@Entity(name = "FagsakProsessTask")
@Table(name = "FAGSAK_PROSESS_TASK")
public class FagsakProsessTask extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAGSAK_PROSESS_TASK")
    private Long id;

    @Column(name = "fagsak_id", nullable = false, updatable = false)
    private Long fagsakId;

    @Column(name = "prosess_task_id", nullable = false, updatable = false)
    private Long prosessTaskId;

    @Column(name = "behandling_id", updatable = false)
    private Long behandlingId;

    @Column(name = "gruppe_sekvensnr", updatable = false)
    private Long gruppeSekvensNr;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    FagsakProsessTask() {
        // Hibernate trenger en
    }

    public FagsakProsessTask(Long fagsakId, Long prosessTaskId, Long behandlingId) {
        this(fagsakId, prosessTaskId, behandlingId, null);
    }

    public FagsakProsessTask(Long fagsakId, Long prosessTaskId, Long behandlingId, Long gruppeSekvensNr) {
        this.fagsakId = fagsakId;
        this.prosessTaskId = prosessTaskId;
        this.behandlingId = behandlingId;
        this.gruppeSekvensNr = gruppeSekvensNr;
    }

    public FagsakProsessTask(Long fagsakId, Long prosessTaskId) {
        this(fagsakId, prosessTaskId, null);
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getProsessTaskId() {
        return prosessTaskId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Long getGruppeSekvensNr() {
        return gruppeSekvensNr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof FagsakProsessTask)) {
            return false;
        }
        FagsakProsessTask other = (FagsakProsessTask) obj;
        return Objects.equals(prosessTaskId, other.prosessTaskId)
                && Objects.equals(fagsakId, other.fagsakId)
                && Objects.equals(behandlingId, other.behandlingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prosessTaskId, fagsakId, behandlingId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
                + "prosessTask=" + prosessTaskId
                + ", fagsak=" + fagsakId
                + (behandlingId == null ? "" : ", behandling=" + behandlingId)
                + (gruppeSekvensNr == null ? "" : ", gruppeSekvensNr=" + gruppeSekvensNr)
                + ">";
    }
}
