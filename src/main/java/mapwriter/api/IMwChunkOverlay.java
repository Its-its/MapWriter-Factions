package mapwriter.api;

import java.awt.Point;

public interface IMwChunkOverlay {
	Point getCoordinates();

	int getColor();

	float getFilling();

	boolean hasBorder();

	float getBorderWidth();

	int getBorderColor();

	// Faction additions
	boolean customBorder();

	void drawCustomBorder(Point.Double topCorner, Point.Double botCorner);
	
	boolean custom();
	
	void drawCustom(Point.Double topCorner, Point.Double botCorner);
}