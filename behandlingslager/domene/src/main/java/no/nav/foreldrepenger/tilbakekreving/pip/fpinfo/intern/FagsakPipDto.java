package no.nav.foreldrepenger.tilbakekreving.pip.fpinfo.intern;

import java.util.Set;

public class FagsakPipDto {

    private String fagsakstatus;
    private Set<String> aktørIder;

    public FagsakPipDto() {}

    public FagsakPipDto(String fagsakstatus, Set<String> aktørIder) {
        this.fagsakstatus = fagsakstatus;
        this.aktørIder = aktørIder;
    }

    public String getFagsakstatus() {
        return fagsakstatus;
    }

    public Set<String> getAktørIder() {
        return aktørIder;
    }

}
