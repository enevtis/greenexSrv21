package graph.greenexSrv2.com;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

public class PngTimelineDiagramPainter {

	public Color diagBackground = new Color(255, 255, 255);

	public int stepRight = 14;
	public int maxPercent = 85;
	public int pictureWidth = 500;
	public int pictureHeight = 400;
	public boolean printLegend = true;
	public boolean printTime = true;
	public boolean isPercentGraph = false;
	public String imgPath = "";
	public String timeFormat = "dd-MM-yy";
	public int maxIndex = -1;
	
	
	private static final int BORDER_GAP = 60;
	private static final int Y_HATCH_CNT = 10;
	private static final int GRAPH_POINT_WIDTH = 12;
	private static final Color GRAPH_COLOR = Color.blue;
	private static final Color GRAPH_POINT_COLOR = new Color(150, 50, 50, 180);
	private static final Stroke GRAPH_STROKE = new BasicStroke(3f);
	
	
	private float maxValue = 0f;
	
	
	public PngTimelineDiagramPainter(){
		
	}
	public String paintTimeLineDiagram(List<GraphTimeValue> tValues, String caption) {
		String out = "";
		out = UUID.randomUUID().toString();

//		out = "111";
		
		BufferedImage img = new BufferedImage(pictureWidth, pictureHeight, BufferedImage.TYPE_INT_BGR);
		stepRight = (int) ((float)(pictureWidth - BORDER_GAP * 2) / (float)tValues.size() -1 );
		
		maxValue = getMaxValue(tValues);
		
		
		try {

			Graphics2D g = img.createGraphics();
			g.setPaint(diagBackground);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());

			g.setPaint(Color.black);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			
			int vertStep = (int) ((float)(pictureHeight - BORDER_GAP)  / (float)maxValue);
			int captionWidth = g.getFontMetrics().stringWidth(caption);
			int captionLeft = pictureWidth/2 - captionWidth/2;
			
			g.drawString(caption, captionLeft, 10);

			
			
			g.drawLine(BORDER_GAP, pictureHeight - BORDER_GAP, BORDER_GAP, BORDER_GAP);
		    g.drawLine(BORDER_GAP, pictureHeight - BORDER_GAP, pictureWidth - BORDER_GAP, pictureHeight - BORDER_GAP);
			

		    
		    float koefVert = (float)(pictureHeight - 2 * BORDER_GAP) / (float) (maxValue * 1.2);
		    
		    
		    float vStep = (float) (maxValue * 1.2) / Y_HATCH_CNT;
		    
		    float vLeftValue = 0;
		    
		    
		      for (int i = 0; i < Y_HATCH_CNT; i++) {
		          int x0 = BORDER_GAP;
		          int x1 = GRAPH_POINT_WIDTH + BORDER_GAP;
		          int y0 = pictureHeight - (((i + 1) * (pictureHeight - BORDER_GAP * 2)) / Y_HATCH_CNT + BORDER_GAP);
		          int y1 = y0;
		          g.drawLine(x0, y0, x1, y1);
		          vLeftValue += vStep;
		          g.drawString(String.format("%.1f", vLeftValue), x0 + 3, y0 - 3);
		       }
		    
		      for (int i = 0; i < tValues.size() - 1 ; i++) {

		    	  int x0 = BORDER_GAP + (i + 1) * stepRight; 
		    	  int x1 = x0;
		          int y0 = pictureHeight - BORDER_GAP;
		          int y1 = y0 - GRAPH_POINT_WIDTH;
		          g.drawLine(x0, y0, x1, y1);
		       } 
		    
			
		      Stroke oldStroke = g.getStroke();
		      g.setColor(GRAPH_COLOR);
		      g.setStroke(GRAPH_STROKE);
		      
		      
			int cTop = pictureHeight - BORDER_GAP;
			int cLeft = BORDER_GAP;
			
			
		    Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                          0, new float[]{3}, 0);			
			
		      
	// x - ��� ����� �������. y - �� ������ ����.		      

	    	  int firstValue = 0;
	    	  int secondValue = 0;
		    
	    	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
		    
		    for (int i = 0; i < tValues.size(); i++) {
		    	 
		    	  if ( i < tValues.size() - 1) {

		    	  firstValue = cTop - (int)(tValues.get(i).tValue * koefVert);
		    	  secondValue = cTop - (int)(tValues.get(i + 1).tValue * koefVert);
		    	  
		    	 
			     g.setColor(GRAPH_COLOR);
			     g.setStroke(GRAPH_STROKE);
			      
			     g.drawLine(cLeft, firstValue, cLeft + stepRight, secondValue);
		    	  
		    	  
		    	  g.setColor(new Color(200,200,200));

		    	  g.setStroke(dashed);
		    	  g.drawLine(cLeft, firstValue, cLeft, cTop);
		    	  
		    	  g.setColor(Color.black);
		    	  
		    	  
		    	// Format LocalDateTime
		    	  String formattedDateTime = tValues.get(i).dateTime.format(formatter);
		    	  drawRotate(g, cLeft, cTop + 5, 45, formattedDateTime);
		    	  
		    	  } else {

		    		  firstValue = cTop - (int)(tValues.get(i).tValue * koefVert);
		    		  
		    		  g.setColor(new Color(200,200,200));

			    	  g.setStroke(dashed);
			    	  g.drawLine(cLeft, firstValue, cLeft, cTop);
			    	  
			    	  g.setColor(Color.black);
			    	  
			    	  String formattedDateTime = tValues.get(i).dateTime.format(formatter);
			    	  drawRotate(g, cLeft, cTop + 5, 45, formattedDateTime);		    		  
		    		  
		    		  
		    		  
		    	  }
		    	  
				 if(i == maxIndex)    g.drawString("max=" + tValues.get(i).tValue, cLeft, firstValue - 3);
		    	  
		         cLeft += stepRight;
		         
		      }
		      
		      

				if (printLegend) {

					g.setPaint(Color.black);

//					String usedData = "" + usedPercent + "% (" + d.usedSizeGb + " �� " + d.maxSizeGb + " ��)";
//					String strLine = usedData + " " + d.path;
//					g.drawString(strLine, cLeft + 160, cTop + stepHeight - 4);
				}

			
			
			
			if (printTime) {
				DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
				String currentTime = df.format(new Date());

				Font currentFont = g.getFont();
				Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.8F);
				g.setFont(newFont);
				
				g.setPaint(Color.black);
				g.drawString(currentTime, BORDER_GAP, this.pictureHeight - 5);
			}
			File output = new File(imgPath + File.separator + out + ".png");
			
			ImageIO.write(img, "png", output);
		} catch (IOException log) {
			System.out.println(log);
		}

		
		
	
		return out;
	}
	public static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) 
	{    
	    
		Font currentFont = g2d.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.8F);
		g2d.setFont(newFont);
		g2d.translate((float)x,(float)y);
	    g2d.rotate(Math.toRadians(angle));
	    g2d.drawString(text,0,0);
	    g2d.rotate(-Math.toRadians(angle));
	    g2d.translate(-(float)x,-(float)y);
	    g2d.setFont(currentFont);
	} 
	
	private float getMaxValue(List<GraphTimeValue> tValues) {
		float out = 0f;
		
//		if (isPercentGraph) return 100f;
		
		for(int i=0; i< tValues.size(); i++) {
			if (tValues.get(i).tValue > out) {
				out = tValues.get(i).tValue;
				maxIndex = i;
			}
		}
		
		return out;
	}
}
