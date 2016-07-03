package mapwriter.overlay;

import java.awt.Point;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import mapwriter.Mw;
import mapwriter.util.Render;
import mapwriter.api.IMwChunkOverlay;
import mapwriter.api.IMwDataProvider;
import mapwriter.fac.Faction;
import mapwriter.fac.FactionInput;
import mapwriter.forge.MwForge;
import mapwriter.gui.MwGui;
import mapwriter.gui.MwGuiFactionDialog;
import mapwriter.gui.MwGuiMarkerDialogNew;
import mapwriter.map.MapView;
import mapwriter.map.mapmode.MapMode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class OverlayFactionGrid extends OverlayFaction {
	
	@Override
	public List<IMwChunkOverlay> getChunksOverlay(int dim, double centerX, double centerZ, double minX, double minZ, double maxX, double maxZ) {
		List<IMwChunkOverlay> chunks = super.getChunksOverlay(dim, centerX, centerZ, minX, minZ, maxX, maxZ);

		// TEMP: Just a easy way to add grids.
		if(Minecraft.getMinecraft().currentScreen instanceof MwGui) {
			chunks.add(new ChunkOverlay(chunkX, chunkZ));
			chunks.add(new ChunkOverlay(chunkX - 1, chunkZ));
			chunks.add(new ChunkOverlay(chunkX + 1, chunkZ));
			chunks.add(new ChunkOverlay(chunkX, chunkZ - 1));
			chunks.add(new ChunkOverlay(chunkX, chunkZ + 1));
		}

		return chunks;
	}

	public class ChunkOverlay implements IMwChunkOverlay {
		Point coord;

		public ChunkOverlay(int x, int z) {
			this.coord = new Point(x, z);
		}

		@Override
		public Point getCoordinates() {
			return this.coord;
		}

		@Override
		public int getColor() {
			return 0x00ffffff;
		}

		@Override
		public float getFilling() {
			return 1.0f;
		}

		@Override
		public boolean hasBorder() {
			return true;
		}

		@Override
		public float getBorderWidth() {
			return 0.5f;
		}

		@Override
		public int getBorderColor() {
			return 0xff000000;
		}

		@Override
		public boolean customBorder() {
			return true;
		}

		@Override
		public void drawCustomBorder(Double topCorner, Double botCorner) {
			float bw = this.getBorderWidth();
			double x = topCorner.x, y = topCorner.y;
			double w = botCorner.x - topCorner.x - bw, h = botCorner.y
					- topCorner.y - bw;

			Render.setColour(this.getBorderColor());
			// N | S | W | E
			Render.drawRect(x - w, y, (w * 3) + bw, bw);
			Render.drawRect(x - w, y + h, (w * 3) + bw, bw);
			Render.drawRect(x, y - h, bw, (h * 3) + bw);
			Render.drawRect(x + w, y - h, bw, (h * 3) + bw);
		}

		@Override
		public boolean custom() {
			return false;
		}

		@Override
		public void drawCustom(Double topCorner, Double botCorner) {}
	}
}
