package com.topcoder.ReviewTest;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.swing.*;

public class VanishingMazeVis {
	static int minS = 10, maxS = 40;

	static final int invalidScore = -1;
	static final int cWildcard = -1;

	int S; // maze size
	int N; // max number you need to reach
	int maxSteps; // max number of steps you're allowed to take
	double power; // power of the multiplier (N_reached/N) in the score

	volatile int[] numbers; // numbers 1..N or -1 if there is no tile
	volatile int pRow, pCol; // row and column coordinates of the player
	volatile int nextNumber; // next number that needs to be hit
	volatile int step; // current step
	volatile double tilesScore; // score of the path taken from the start to the last target number hit
	volatile double pendingScore; // pending part of the score: score from the last target number hit to the
									// current step
	// -----------------------------------------

	String generate(String seedStr) {
		try {
			// generate test case
			SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG");
			long seed = Long.parseLong(seedStr);
			r1.setSeed(seed);
			S = r1.nextInt(maxS - minS + 1) + minS;
			if (seed == 1)
				S = minS;
			else if (seed == 2)
				S = (2 * minS + maxS) / 3;
			else if (seed == 3)
				S = maxS;

			int minN = S, maxN = S * S / 4;
			N = r1.nextInt(maxN - minN + 1) + minN;
			if (seed == 1)
				N = minN;
			else if (seed == 2)
				N = (2 * minN + maxN) / 3;
			else if (seed == 3)
				N = maxN;

			// generate the numbers in the maze
			numbers = new int[S * S];
			// to make sure each number from 1 to N is present at least once, add them up
			// front
			for (int i = 1; i <= N; ++i)
				numbers[i - 1] = i;
			// add a certain number of holes
			int nHoles = r1.nextInt((S * S - N) / 5);
			for (int i = 0; i < nHoles; ++i)
				numbers[N + i] = 0;
			// add a certain number of wildcards (-1)
			int nWildcards = r1.nextInt((S * S - N) / 10);
			for (int i = 0; i < nWildcards; ++i)
				numbers[N + nHoles + i] = cWildcard;
			// fill the rest of the tiles with random numbers
			for (int i = N + nHoles + nWildcards; i < S * S; ++i)
				numbers[i] = r1.nextInt(N) + 1;

			// do a random permutation of these tiles
			for (int i = 0; i < S * S - 1; ++i) {
				int j = r1.nextInt(S * S - i) + i;
				int tmp = numbers[i];
				numbers[i] = numbers[j];
				numbers[j] = tmp;
			}

			// generate the starting position of the player (make sure it's not on a special
			// tile or on a 1 tile)
			do {
				pRow = r1.nextInt(S);
				pCol = r1.nextInt(S);
			} while (numbers[pRow * S + pCol] <= 1);

			power = r1.nextDouble() * 2.0;

			StringBuffer sb = new StringBuffer();
			sb.append("S = ").append(S).append('\n');
			sb.append("N = ").append(N).append('\n');
			sb.append("Number of holes = ").append(nHoles).append('\n');
			sb.append("Number of wildcards = ").append(nWildcards).append('\n');
			for (int i = 0; i < S; ++i) {
				for (int j = 0; j < S; ++j)
					sb.append(numbers[i * S + j] + " ");
				sb.append('\n');
			}
			sb.append("Starting position = (" + pRow + ", " + pCol + ")\n");
			sb.append("(N_reached/N) power = " + power + "\n");
			return sb.toString();
		} catch (Exception e) {
			addFatalError("An exception occurred while generating test case.");
			e.printStackTrace();
			return "";
		}
	}

	// -----------------------------------------
	void takeAction(char act) {
		int newRow = pRow, newCol = pCol;
		if (act == 'U')
			newRow = (newRow + S - 1) % S;
		else if (act == 'D')
			newRow = (newRow + 1) % S;
		else if (act == 'L')
			newCol = (newCol + S - 1) % S;
		else if (act == 'R')
			newCol = (newCol + 1) % S;
		else {
			addFatalError("Ignoring unknown action '" + act + "'.");
			return;
		}
		if (numbers[newRow * S + newCol] == 0) {
			addFatalError("Ignoring move to a hole at (" + newRow + ", " + newCol + ").");
			return;
		}
		pRow = newRow;
		pCol = newCol;

		// when you move to a new tile, first of all apply the wildcard effect
		if (numbers[pRow * S + pCol] == cWildcard) {
			numbers[pRow * S + pCol] = nextNumber;
		}

		// check whether the next tile you step on is the target one
		if (numbers[pRow * S + pCol] == nextNumber) {
			nextNumber++;
			tilesScore += pendingScore;
			pendingScore = 0;
		}
		// destroy tiles > nextNumber and add them to the pending score (with nextNumber
		// multiplier)
		if (numbers[pRow * S + pCol] > nextNumber) {
			pendingScore += nextNumber * numbers[pRow * S + pCol];
			numbers[pRow * S + pCol] = 0;
		}
	}

	// -----------------------------------------
	public double runTest(String seed) {
		try {
			String test = generate(seed);
			if (debug)
				System.out.println(test);

			step = 0;
			nextNumber = 1;
			tilesScore = 0;
			pendingScore = 0;
			maxSteps = 4 * N * S;

			if (vis) {
				jf.setVisible(true);
				Insets frameInsets = jf.getInsets();
				int fw = frameInsets.left + frameInsets.right + 8;
				int fh = frameInsets.top + frameInsets.bottom + 8;
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Dimension screenSize = toolkit.getScreenSize();
				Insets screenInsets = toolkit.getScreenInsets(jf.getGraphicsConfiguration());
				screenSize.width -= screenInsets.left + screenInsets.right;
				screenSize.height -= screenInsets.top + screenInsets.bottom;
				if (SZ == 0) {
					SZ = Math.min((screenSize.width - fw - 120) / S, (screenSize.height - fh) / S);
				}
				Dimension dim = v.getVisDimension();
				v.setPreferredSize(dim);
				jf.setSize(Math.min(dim.width + fw, screenSize.width), Math.min(dim.height + fh, screenSize.height));
				manualReady = false;
				draw();
			}

			if (!manual) {
				String pathRet;
				try {
					pathRet = getPath(numbers, pRow, pCol, power);
				} catch (Exception e) {
					addFatalError("Failed to get result from getPath.");
					return invalidScore;
				}

				// validate and perform the action
				if (pathRet == null) {
					addFatalError("Your return was empty.");
					return invalidScore;
				}
				if (pathRet.length() > maxSteps) {
					addFatalError("Your can take at most " + maxSteps + " steps.");
					return invalidScore;
				}
				// perform the moves
				for (step = 1; step <= pathRet.length() && nextNumber <= N; ++step) {
					takeAction(pathRet.charAt(step - 1));
					if (vis) {
						draw();
					}
				}
			} else {
				// in manual mode, let the player do up to maxSteps key presses
				for (step = 1; step <= maxSteps && !manualReady && nextNumber <= N; ++step) {
					keyPressed = false;
					manualMove = '?';
					while (!keyPressed) {
						try {
							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					takeAction(manualMove);
					if (vis) {
						draw();
					}
				}
			}

			// pending score is accumulated only when a goal is reached, not in the end
			return tilesScore * Math.pow((nextNumber - 1) * 1.0 / N, power);
		} catch (Exception e) {
			addFatalError("An exception occurred while trying to get your program's results.");
			e.printStackTrace();
			return invalidScore;
		}
	}

// ------------- visualization part ------------
	static String seed;
	JFrame jf;
	Vis v;
	public static String exec;
	public static boolean vis, manual, debug;
	static Process proc;
	InputStream is;
	OutputStream os;
	BufferedReader br;
	static int SZ, delay;
	volatile boolean manualReady;
	volatile char manualMove;
	final Object keyMutex = new Object();
	boolean keyPressed;

	// -----------------------------------------
	String getPath(int[] numbers, int playerRow, int playerCol, double power) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(numbers.length).append("\n");
		for (int i = 0; i < numbers.length; ++i) {
			sb.append(numbers[i]).append("\n");
		}
		sb.append(playerRow).append("\n");
		sb.append(playerCol).append("\n");
		sb.append(power).append("\n");
		os.write(sb.toString().getBytes());
		os.flush();

		// and get the return value
		String ret = br.readLine();
		return ret;
	}

	// -----------------------------------------
	void draw() {
		if (!vis)
			return;
		v.repaint();
		try {
			Thread.sleep(delay);
		} catch (Exception e) {
		}
	}

	// -----------------------------------------
	public class Vis extends JPanel {
		public void paint(Graphics g) {
			super.paint(g);
			Dimension dim = getVisDimension();
			BufferedImage bi = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) bi.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// background
			g2.setColor(new Color(0xDDDDDD));
			g2.fillRect(0, 0, dim.width, dim.height);
			// maze
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, S * SZ, S * SZ);
			g2.setBackground(Color.WHITE);

			// write current numbers on tiles and mark holes in the maze
			g2.setFont(new Font("Arial", Font.BOLD, SZ / 3 + 1));
			FontMetrics fm = g2.getFontMetrics();
			char[] ch;
			for (int r = 0; r < S; ++r)
				for (int c = 0; c < S; ++c) {
					int n = numbers[r * S + c];
					if (n != 0) {
						// mark the tiles which are not going to disappear (but also don't bring any
						// score)
						if (n < nextNumber && n != cWildcard) {
							g2.setColor(new Color(0xDDDDDD));
							g2.fillRect(c * SZ, r * SZ, SZ, SZ);
						}
						// highlight tiles which have the next number and wildcards
						g2.setColor(n == nextNumber || n == cWildcard ? new Color(0xEE0000) : Color.BLACK);
						ch = (n == cWildcard ? "?" : ("" + n)).toCharArray();
						int h = r * SZ + SZ / 2 + fm.getHeight() / 2 - 3;
						int w = fm.charsWidth(ch, 0, ch.length);
						g2.drawChars(ch, 0, ch.length, c * SZ + SZ / 2 - w / 2 + 1, h);
					} else {
						// mark a hole
						g2.setColor(new Color(0x888888));
						g2.fillRect(c * SZ, r * SZ, SZ, SZ);
					}
				}

			// highlight current player position
			g2.setStroke(new BasicStroke(2f));
			g2.setColor(Color.BLUE);
			g2.drawRect(pCol * SZ + 2, pRow * SZ + 2, SZ - 4, SZ - 4);

			// lines between cells
			g2.setStroke(new BasicStroke(1f));
			g2.setColor(Color.BLACK);
			for (int i = 0; i <= S; i++)
				g2.drawLine(0, i * SZ, S * SZ, i * SZ);
			for (int i = 0; i <= S; i++)
				g2.drawLine(i * SZ, 0, i * SZ, S * SZ);

			// status info
			g2.setFont(new Font("Arial", Font.BOLD, 13));

			int xText = SZ * S + 10;
			int wText = 100;
			int yText = 10;
			int hText = 20;
			int vGap = 10;

			// current score
			drawString(g2, "SCORE", xText, yText, wText, hText, 0);
			yText += hText;
			drawString(g2, String.format("%.2f", tilesScore * Math.pow((nextNumber - 1) * 1.0 / N, power)), xText,
					yText, wText, hText, 0);
			yText += hText * 2;

			// pending score (not taking into account the power modifier)
			drawString(g2, "Pending:", xText, yText, wText, hText, 0);
			yText += hText;
			drawString(g2, String.format("%.2f", pendingScore), xText, yText, wText, hText, 0);
			yText += hText * 2;

			// next number
			drawString(g2, "Done:", xText, yText, wText * 2 / 3, hText, 1);
			drawString(g2, (nextNumber - 1) + "/" + N, xText + wText * 2 / 3 + 2, yText, wText / 3, hText, -1);
			yText += hText;
			if (nextNumber <= N) {
				drawString(g2, "Next:", xText, yText, wText * 2 / 3, hText, 1);
				drawString(g2, nextNumber + "", xText + wText * 2 / 3 + 2, yText, wText / 3, hText, -1);
			}
			yText += hText * 2;
			// step
			drawString(g2, "Step:", xText, yText, wText * 2 / 3, hText, 1);
			drawString(g2, String.valueOf(step), xText + wText * 2 / 3 + 2, yText, wText / 3, hText, -1);
			yText += hText;

			g.drawImage(bi, 0, 0, null);
		}

		// -------------------------------------
		void drawString(Graphics2D g2, String text, int x, int y, int w, int h, int align) {
			FontMetrics metrics = g2.getFontMetrics();
			Rectangle2D rect = metrics.getStringBounds(text, g2);
			int th = (int) (rect.getHeight());
			int tw = (int) (rect.getWidth());
			if (align == 0)
				x = x + (w - tw) / 2;
			else if (align > 0)
				x = (x + w) - tw;
			y = y + (h - th) / 2 + metrics.getAscent();
			g2.drawString(text, x, y);
		}

		// -------------------------------------
		public Vis() {
			jf.addWindowListener(new DrawerWindowListener());
			jf.addKeyListener(new KeyboardListener());
		}

		// -------------------------------------
		public Dimension getVisDimension() {
			return new Dimension(S * SZ + 126, Math.max(S * SZ + 1, 550));
		}
	}

	// -----------------------------------------
	class DrawerWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent event) {
			if (proc != null)
				try {
					proc.destroy();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			System.exit(0);
		}
	}

	// -----------------------------------------
	class KeyboardListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			synchronized (keyMutex) {
				int keyCode = e.getKeyCode();
				switch (keyCode) {
				case KeyEvent.VK_ESCAPE:
					manualReady = true;
					break;
				case KeyEvent.VK_UP:
					manualMove = 'U';
					break;
				case KeyEvent.VK_DOWN:
					manualMove = 'D';
					break;
				case KeyEvent.VK_LEFT:
					manualMove = 'L';
					break;
				case KeyEvent.VK_RIGHT:
					manualMove = 'R';
					break;
				}
				keyPressed = true;
				keyMutex.notifyAll();
			}
		}
	}

	// -----------------------------------------
	public double score = -1.0;
	public static ErrorReader output;

	public VanishingMazeVis(String seed) {
		try {
			if (vis) {
				jf = new JFrame();
				jf.setTitle("Seed " + seed);
				v = new Vis();
				JScrollPane sp = new JScrollPane(v);
				jf.getContentPane().add(sp);
			}
			if (exec != null) {
				try {
					Runtime rt = Runtime.getRuntime();
					proc = rt.exec(exec);
					os = proc.getOutputStream();
					is = proc.getInputStream();
					br = new BufferedReader(new InputStreamReader(is));
					output = new ErrorReader(proc.getErrorStream());
					output.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			score = runTest(seed);
			if (proc != null)
				try {
					proc.destroy();
				} catch (Exception e) {
					e.printStackTrace();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------------
	public static double main(String[] args) {
        seed = "1";
        vis = false;
        manual = false;
        SZ = 0; //Auto-fit to desktop size
        delay = 100;
        for (int i = 0; i<args.length; i++)
        {   
        	if (args[i].equals("-seed"))
                seed = args[++i];
            if (args[i].equals("-exec"))
                exec = args[++i];
            if (args[i].equals("-novis"))
                vis = false;
            if (args[i].equals("-manual"))
                manual = true;
            if (args[i].equals("-size"))
                SZ = Integer.parseInt(args[++i]);
            if (args[i].equals("-debug"))
                debug = true;
            if (args[i].equals("-delay"))
                delay = Integer.parseInt(args[++i]);
        }
        
        if (exec == null)
            manual = true;
        if (manual)
            vis = true;
        
       
        VanishingMazeVis f = new VanishingMazeVis(seed);
        return f.score;
    }

	// -----------------------------------------
	void addFatalError(String message) {
		String prefix = (step == 0 ? "Initialization" : ("Step #" + step));
		System.out.println(prefix + ": " + message);
	}
}

class ErrorReader extends Thread {
	InputStream error;
	StringBuilder sb = new StringBuilder();

	public ErrorReader(InputStream is) {
		error = is;
	}

	public void run() {
		try {
			byte[] ch = new byte[50000];
			int read;
			while ((read = error.read(ch)) > 0) {
				String s = new String(ch, 0, read);
				System.out.print(s);
				sb.append(s);
				System.out.flush();
			}
		} catch (Exception e) {
		}
	}

	public String getOutput() {
		return sb.toString();
	}
}
