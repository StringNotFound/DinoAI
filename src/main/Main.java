package main;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


// (475, 225)
// (475, 206)
// (518, ...)

// 83, 247

public class Main {
	
	static Robot r;
	
	static final int threshold = 165;
	static final int backgroundX = 165;
	static final int backgroundY = 337;
	
	static final int lowX = 475;
	static final int lowY = 215;
	static final int highX = 475;
	static final int highY = 190;
	static final int width = 40;
	static final int height = 5;
	
	static final int shortJumpTime = 100;
	static final int duckTime = 400;
	static final int longJumpTime = 200;
	
	static int speed = 0;
	

	public static void main(String[] args) {

		try {
			r = new Robot();
		} catch (AWTException e) {
			System.err.println("Could not create robot");
			System.exit(1);
		}

		playGame();
		//trackMouse();
		
	}
	
	public static void trackMouse() {
		while (true) {
			Point pt = MouseInfo.getPointerInfo().getLocation();
			System.out.println("(" + pt.getX() + ", " + pt.getY() + ")");
			
			Color color = r.getPixelColor((int) pt.getX(), (int) pt.getY());
			System.out.println(color.getRed());
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	public static void playGame() {
		
		r.mouseMove(lowX, lowY);
		boolean wasDay = true;
		
		while (true) {
			
			boolean isDay = isDay();

			if (!wasDay && isDay) {
				speed = Math.min(speed + 1, 3);
			}
			
			wasDay = isDay;

			BufferedImage highImg = r.createScreenCapture(new Rectangle(highX, highY, width + speed * 7, height));
			boolean highObstical = isObstical(highImg);
			
			int lowLookAhead = 0;
			if (highObstical) {
			}
			lowLookAhead = 10;
			
			BufferedImage lowImg = r.createScreenCapture(new Rectangle(lowX, lowY, width + speed * 7 + lowLookAhead, height));
			boolean lowObstical = isObstical(lowImg);

			dodge(lowObstical, highObstical);
			
		}
		
	}
	
	public static void dodge(boolean lowObstical, boolean highObstical) {
		if (lowObstical && highObstical) {
			System.out.println("long jump");
			jump(longJumpTime, 0);
			return;
		}
		if (lowObstical && !highObstical) {
			System.out.println("short jump");
			jump(shortJumpTime, speed);
			return;
		}
		if (!lowObstical && highObstical) {
			System.out.println("duck");
			duck(duckTime);
			return;
		}
		if (!lowObstical && !highObstical) {
			return;
		}
	}
	
	public static void jump(int time, int downWaitTime) {
		r.keyPress(KeyEvent.VK_UP);
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			return;
		}
		r.keyRelease(KeyEvent.VK_UP);

		if (downWaitTime > 0) {
			try {
				Thread.sleep(time + downWaitTime);
			} catch (InterruptedException e) {
				return;
			}

			r.keyPress(KeyEvent.VK_DOWN);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			r.keyRelease(KeyEvent.VK_DOWN);
		}
	}

	public static void duck(int time) {
		r.keyPress(KeyEvent.VK_DOWN);
		try {
			int newSpeed = Math.min(speed, 2);
			Thread.sleep((long) (time/(1 + 0.5 * newSpeed)));
		} catch (InterruptedException e) {
			return;
		}
		r.keyRelease(KeyEvent.VK_DOWN);
	}
	
	public static boolean isDay() {
		return r.getPixelColor(backgroundX, backgroundY).getRed() > threshold;
	}
	
	/*public static boolean isObstical(boolean isDay, BufferedImage img) {
		boolean obstical = false;
		for (int x = 0; x < img.getWidth() && !obstical; x++) {
			for (int y = 0; y < img.getHeight() && !obstical; y++) {
				if (isDay) {
					obstical |= new Color(img.getRGB(x, y)).getRed() < threshold;
				} else {
					obstical |= new Color(img.getRGB(x, y)).getRed() > threshold;
				}
			}
		}
		
		return obstical;
	}*/
	
	public static boolean isObstical(BufferedImage img) {
		boolean obstical = false;
		//int same = new Color(img.getRGB(0, 0)).getRed();
		int background = r.getPixelColor(backgroundX, backgroundY).getRed();
		for (int x = 0; x < img.getWidth() && !obstical; x++) {
			for (int y = 0; y < img.getHeight() && !obstical; y++) {
				obstical |= Math.abs(new Color(img.getRGB(x, y)).getRed() - background) > 10;
			}
		}
		
		return obstical;
	}

}