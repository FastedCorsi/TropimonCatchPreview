package fr.tropimon.catchpreview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WindowPositionTest {
    @Test void defaultPositionIsUnchanged() {
        var p = new WindowPosition(150, 101);
        assertEquals(638, p.left(800)); assertEquals(348, p.top(600));
    }
    @Test void dragPositionSurvivesStopAndNextDragWithoutJumping() {
        var p = new WindowPosition(150, 101);
        assertTrue(p.start(650, 360, 800, 600));
        assertTrue(p.drag(200, 150, 800, 600));
        assertEquals(188, p.left(800)); assertEquals(138, p.top(600));
        assertTrue(p.stop()); assertFalse(p.drag(500, 500, 800, 600));
        assertEquals(188, p.left(800));
        assertTrue(p.start(200, 150, 800, 600));
        p.drag(200, 150, 800, 600);
        assertEquals(188, p.left(800)); assertEquals(138, p.top(600));
    }
    @Test void releaseAndCloseButtonsAndPortraitAreNotDragHandles() {
        var p = new WindowPosition(150, 101);
        assertFalse(p.start(750, 358, 800, 600));
        assertFalse(p.start(775, 358, 800, 600));
        assertFalse(p.start(650, 390, 800, 600));
    }
    @Test void draggingAndWindowResizeStayWithinBounds() {
        var p = new WindowPosition(150, 101);
        p.start(650, 360, 800, 600); p.drag(-100, -100, 800, 600);
        assertEquals(2, p.left(800)); assertEquals(2, p.top(600));
        p.drag(900, 900, 800, 600);
        assertEquals(648, p.left(800)); assertEquals(497, p.top(600));
        assertEquals(168, p.left(320)); assertEquals(137, p.top(240));
    }
}
