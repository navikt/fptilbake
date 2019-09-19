package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

public class KanBehandlingOpprettesDto {

    private boolean kanBehandlingOpprettes;
    private boolean kanRevurderingOpprettes;

    public boolean isKanBehandlingOpprettes() {
        return kanBehandlingOpprettes;
    }

    public void setKanBehandlingOpprettes(boolean kanBehandlingOpprettes) {
        this.kanBehandlingOpprettes = kanBehandlingOpprettes;
    }

    public boolean isKanRevurderingOpprettes() {
        return kanRevurderingOpprettes;
    }

    public void setKanRevurderingOpprettes(boolean kanRevurderingOpprettes) {
        this.kanRevurderingOpprettes = kanRevurderingOpprettes;
    }
}
