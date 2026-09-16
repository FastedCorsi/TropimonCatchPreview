package fr.tropimon.catchpreview;

/** Pure layout/drag state; no mouse locking and no dependency on another mod's config. */
final class WindowPosition {
    private final int width, height;
    private boolean positioned, dragging;
    private int x, y;
    private double offsetX, offsetY;
    WindowPosition(int width, int height) { this.width = width; this.height = height; }
    int left(int screenWidth) {
        return positioned ? clamp(x, screenWidth, width) : Math.max(2, screenWidth - width - 12);
    }
    int top(int screenHeight) {
        return positioned ? clamp(y, screenHeight, height)
                : Math.min(Math.max(2, screenHeight - height - 2), Math.max(12, (int) (screenHeight * .58F)));
    }
    boolean start(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        int left = left(screenWidth), top = top(screenHeight);
        // Header only; leave the release and close buttons untouched, even when hidden.
        if (mouseX < left + 7 || mouseX >= left + width - 49 || mouseY < top + 4 || mouseY >= top + 20) return false;
        offsetX = mouseX - left;
        offsetY = mouseY - top;
        x = left; y = top;
        positioned = dragging = true;
        return true;
    }
    boolean drag(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        if (!dragging) return false;
        x = clamp((int) Math.round(mouseX - offsetX), screenWidth, width);
        y = clamp((int) Math.round(mouseY - offsetY), screenHeight, height);
        return true;
    }
    boolean stop() { boolean wasDragging = dragging; dragging = false; return wasDragging; }
    private static int clamp(int coordinate, int screenSize, int size) {
        return Math.max(2, Math.min(coordinate, Math.max(2, screenSize - size - 2)));
    }
}
