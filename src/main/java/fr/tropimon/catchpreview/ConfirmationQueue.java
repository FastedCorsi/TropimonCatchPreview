package fr.tropimon.catchpreview;

import java.util.function.LongSupplier;

/** Normal previews expire; a first release click locks its target without a timeout. */
final class ConfirmationQueue<T> {
    private static final long DISPLAY_NS = 20_000_000_000L;
    private final LongSupplier clock;
    private long shownAt;
    T visible, queued, pending;
    ConfirmationQueue() { this(System::nanoTime); }
    ConfirmationQueue(LongSupplier clock) { this.clock = clock; }
    void show(T value) {
        if (pending != null) queued = value;
        else { visible = value; queued = null; shownAt = clock.getAsLong(); }
    }
    void confirm() { pending = visible; }
    void complete(boolean submitted) {
        pending = null;
        if (submitted || queued != null) { visible = queued; queued = null; }
        shownAt = clock.getAsLong();
    }
    boolean expire() {
        if (visible == null || pending != null || clock.getAsLong() - shownAt < DISPLAY_NS) return false;
        close();
        return true;
    }
    void close() { visible = queued = pending = null; shownAt = 0; }
}
