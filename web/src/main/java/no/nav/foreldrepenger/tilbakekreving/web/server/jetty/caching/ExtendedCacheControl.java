package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.caching;

/**
 * Lager extended cache control for å støtte fleire cache header felt enn jakarta sin innebygde.
 */
public class ExtendedCacheControl extends jakarta.ws.rs.core.CacheControl {
    public static final String IMMUTABLE = "immutable";
    public static final String STALE_WHILE_REVALIDATE = "stale-while-revalidate";
    public static final String STALE_IF_ERROR = "stale-if-error";

    private void addExtension(final String key, final String val) {
        if(key != null && !key.isBlank()) {
            this.getCacheExtension().put(key, val);
        }
    }

    private void addBoolExtension(final String key, final boolean val) {
        if(val) {
            this.addExtension(key, null);
        }
    }

    private void addIntExtension(final String key, final int val) {
        this.addExtension(key, String.valueOf(val));
    }

    private int getIntExtension(final String key) {
        final String v = this.getCacheExtension().get(key);
        if(v != null) {
            return Integer.parseInt(v);
        }
        return 0;
    }

    public void setImmutable(final boolean immutable) {
        this.addBoolExtension(IMMUTABLE, immutable);
    }

    public boolean isImmutable() {
        return this.getCacheExtension().containsKey(IMMUTABLE);
    }

    public void setStaleWhileRevalidate(final int seconds) {
        if(seconds > 0) {
            this.addIntExtension(STALE_WHILE_REVALIDATE, seconds);
        }
    }

    public int getStaleWhileRevalidate() {
        return this.getIntExtension(STALE_WHILE_REVALIDATE);
    }

    public void setStaleIfError(final int seconds) {
        if(seconds > 0) {
            this.addIntExtension(STALE_IF_ERROR, seconds);
        }
    }

    public int getStaleIfError() {
        return this.getIntExtension(STALE_IF_ERROR);
    }
}
