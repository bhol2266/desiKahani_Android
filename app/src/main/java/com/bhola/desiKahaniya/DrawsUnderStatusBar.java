package com.bhola.desiKahaniya;

/**
 * Marker for activities whose own artwork is meant to run behind the status bar.
 *
 * MyApplication applies system-bar insets as padding to every activity root so
 * content stays clear of the bars under edge-to-edge. Activities implementing
 * this skip the TOP inset only - they reclaim that vertical space for their
 * hero artwork - and still receive the bottom/side insets.
 */
public interface DrawsUnderStatusBar {
}
