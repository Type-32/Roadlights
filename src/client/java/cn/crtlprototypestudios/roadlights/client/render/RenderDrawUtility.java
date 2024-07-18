package cn.crtlprototypestudios.roadlights.client.render;

import net.minecraft.client.gui.DrawContext;

public class RenderDrawUtility {
    public static void drawArrow(DrawContext drawContext, int[] xPoints, int[] yPoints, int color) {
        // Draw the outline of the arrow
        drawTriangle(drawContext, xPoints, yPoints, color);

        // Fill the arrow
        fillTriangle(drawContext, xPoints, yPoints, color);
    }

    public static void drawLine(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        if (dx >= dy) {
            // More horizontal
            if (x1 > x2) {
                int temp = x1; x1 = x2; x2 = temp;
                temp = y1; y1 = y2; y2 = temp;
            }
            for (int x = x1; x <= x2; x++) {
                int y = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
                drawContext.drawVerticalLine(x, y, y, color);
            }
        } else {
            // More vertical
            if (y1 > y2) {
                int temp = x1; x1 = x2; x2 = temp;
                temp = y1; y1 = y2; y2 = temp;
            }
            for (int y = y1; y <= y2; y++) {
                int x = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
                drawContext.drawHorizontalLine(x, x, y, color);
            }
        }
    }

    public static void fillTriangle(DrawContext drawContext, int[] xPoints, int[] yPoints, int color) {
        int minY = Math.min(Math.min(yPoints[0], yPoints[1]), yPoints[2]);
        int maxY = Math.max(Math.max(yPoints[0], yPoints[1]), yPoints[2]);

        for (int y = minY; y <= maxY; y++) {
            int x1 = Integer.MAX_VALUE;
            int x2 = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                int j = (i + 1) % 3;
                if ((yPoints[i] <= y && yPoints[j] > y) || (yPoints[j] <= y && yPoints[i] > y)) {
                    int x = xPoints[i] + (y - yPoints[i]) * (xPoints[j] - xPoints[i]) / (yPoints[j] - yPoints[i]);
                    x1 = Math.min(x1, x);
                    x2 = Math.max(x2, x);
                }
            }

            if (x1 <= x2) {
                drawContext.drawHorizontalLine(x1, x2, y, color);
            }
        }
    }

    public static void drawTriangle(DrawContext drawContext, int[] xPoints, int[] yPoints, int color) {
        drawLine(drawContext, xPoints[0], yPoints[0], xPoints[1], yPoints[1], color);
        drawLine(drawContext, xPoints[1], yPoints[1], xPoints[2], yPoints[2], color);
        drawLine(drawContext, xPoints[2], yPoints[2], xPoints[0], yPoints[0], color);
    }
}
