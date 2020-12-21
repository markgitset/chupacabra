package net.markdrew.chupacabra.cli

import org.jline.terminal.TerminalBuilder

/**
 * Returns the current width, in columns, of the user's terminal, or 0 if it can't be determined
 */
fun terminalWidth(): Int = TerminalBuilder.builder()
    .dumb(true) // this is necessary to avoid the dumb terminal warning--it doesn't force a dumb terminal
    .build().use { it.width }
