package graph.greenexSrv2.com;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class PngDisksDiagramPainter {

	public final Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	public Color diagBackground = new Color(255, 255, 255);

	public int stepHeight = 14;
	public int maxPercent = 85;
	public int pictureWidth = 450;
	public int indicatorWidth = 120;
	public boolean printLegend = true;
	public boolean printTime = true;
	public String imgPath = "";

	public PngDisksDiagramPainter() {
	}

	

	public String paintDisksDiagram(List<GraphDisk> disks, String caption) {
		String out = "";

		int pictureHeight = stepHeight * disks.size() + 40;

		BufferedImage img = new BufferedImage(pictureWidth, pictureHeight, BufferedImage.TYPE_INT_BGR);

		out = UUID.randomUUID().toString();

		try {

			Graphics2D g = img.createGraphics();
			g.setPaint(diagBackground);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());

			g.setPaint(Color.black);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(caption, 50, 10);

			int cTop = 20;
			int cLeft = 20;
			int usedPercent = 0;
			int usedWidth = 0;

			for (GraphDisk d : disks) {

				g.setPaint(Color.darkGray);
				g.drawRect(cLeft, cTop, indicatorWidth, 10);

				usedPercent = (int) ((float) d.usedSizeGb / (float) d.maxSizeGb * 100);
				usedWidth = (int) ((float) d.usedSizeGb / (float) d.maxSizeGb * indicatorWidth);

				Color fillIndicatorColor = Color.blue;

				if (d.minUsedSizeGb == 0) {

					if (usedPercent > maxPercent)
						fillIndicatorColor = Color.red;

				} else {

					if ((d.maxSizeGb - d.usedSizeGb) < d.minUsedSizeGb)
						fillIndicatorColor = Color.red;

				}

				g.setPaint(fillIndicatorColor);

				g.fillRect(cLeft, cTop, usedWidth, 10);

				if (printLegend) {

					g.setPaint(Color.black);

					String formatString = d.maxSizeGb < 300f ? "%.1f": "%.0f";
					String usedData = "" + usedPercent + "% (" + String.format(formatString,d.usedSizeGb) 
					+ " from " + String.format(formatString,d.maxSizeGb) + " Gb)";
					String strLine = usedData + " " + d.path;
					g.drawString(strLine, cLeft + indicatorWidth + 3, cTop + stepHeight - 4);
				}

				cTop += stepHeight;

			}

			if (printTime) {
				DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
				String currentTime = df.format(new Date());

				Font currentFont = g.getFont();
				Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.8F);
				g.setFont(newFont);
				
				g.setPaint(Color.black);
				g.drawString(currentTime, cLeft, cTop + stepHeight - 4);
			}
			File output = new File(imgPath + File.separator + out + ".png");
			ImageIO.write(img, "png", output);
		} catch (IOException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			this.logger.severe(errors.toString());
		}

		return out;
	}

}
