package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import javax.enterprise.util.AnnotationLiteral;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;

/**
 * AnnotationLiteral som kan brukes i CDI søk.
 * <p>
 * Eks. for bruk i:<br>
 * {@link CDI#current#select(javax.enterprise.util.TypeLiteral, java.lang.annotation.Annotation...)}.
 */
class BehandlingStegTypeAnnotationLiteral extends AnnotationLiteral<BehandlingStegRef> implements BehandlingStegRef {

    private String stegKode;
    
    BehandlingStegTypeAnnotationLiteral(String stegKode) {
        this.stegKode = stegKode;
    }
    
    @Override
    public String kode() {
        return stegKode;
    }
    
}