import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;


import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


@SuppressWarnings("serial")
public class Game extends JFrame{
    
	private GameBoard board;
    private GameMenuBar menuBar;
    
	public Game() {
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		setTitle("FlippyBall by Yongqiang Chen");
		setScore();
		
        menuBar = new GameMenuBar();
        setJMenuBar(menuBar);
		
		board = new GameBoard(0);
		getContentPane().add(board);

	}
	
	
	private void setScore() {
    	sText.setOpaque(false);
    	sText.setSize(200, 24);
    	sText.setEditable(false);
    	sText.setFont(new Font("Dialog", Font.PLAIN, 15));
    	sText.addKeyListener(keyListener);
	}
	
	private boolean gameStart = false;
    
	public static void test() {
		Game g = new Game();
		g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		g.setVisible(true);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				test();
			}
		});
	}
	
	private void start() {
		if (gameStart) return;
		score = 0;
		updateScore();
		gameStart = true;
		board.init();
		board.repaint();
		Runnable r = new GameBoardRunnable(board);
		Thread t = new Thread(r);
		t.start();
	}
	
	private int score = 0;
	private JTextArea sText = new JTextArea("Score: " + score);
	
	private class GameMenuBar extends JMenuBar{
		private JMenu game;
		private MyMenuItem easy, medium, hard, exit;
		
	    public GameMenuBar() {
	    	game = new JMenu("Game");  
	    	easy =   new MyMenuItem("Easy                            E"); 
	    	medium = new MyMenuItem("Medium                      M"); 
	    	hard =   new MyMenuItem("Hard                            H"); 
	    	exit =   new MyMenuItem("Exit                             X");
	    	
	    	easy.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			setEasy();
	    		}
	    	});
	    	medium.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			setMedium();
	    		}
	    	});
	    	hard.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			setHard();
	    		}
	    	});
	    	exit.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			System.exit(0);
	    		}
	    	});
	    	
	    	game.add(easy);
	    	game.add(medium);
	    	game.add(hard);
	    	game.addSeparator();
	    	game.add(exit);
	    	game.addKeyListener(keyListener);
	    	game.setFont(new Font("Dialog", Font.PLAIN, 14));
	    	add(game, BorderLayout.PAGE_START);
	    	
	    	JTextArea blank = new JTextArea(" ");
	    	blank.setEnabled(false);
	    	blank.setOpaque(false);
	    	blank.addKeyListener(keyListener);
	    	add(blank, BorderLayout.CENTER);
	    	
	    	
	    	add(sText, BorderLayout.PAGE_END);
	    	
	    	addKeyListener(keyListener);
	    }
	}
	
	private void setEasy() {
		ballDrop = 2;
		doorMove = 1;
		delay = 7;
		paramRandBlank = 10;
		start();
	}
	
	private void setMedium() {
		ballDrop = 3;
		doorMove = 2;
		delay = 6;
		paramRandBlank = 30;
		start();
	}
	
	private void setHard() {
		ballDrop = 4;
		doorMove = 3;
		delay = 6;
		paramRandBlank = 60;
		start();
	}
	
	
	private class MyMenuItem extends JMenuItem {
		public MyMenuItem(String str) {
			super(str);
			setFont(new Font("Dialog", Font.PLAIN, 10));
			addKeyListener(keyListener);
		}
	}
	
	
	private class GameBoardRunnable implements Runnable{
		private GameBoard board;
		
		public GameBoardRunnable(GameBoard b) {
			board = b;
		}
		
		
		public void run() {
			long timer = System.currentTimeMillis();
			try {
				while (board.isAlive() ) {
					board.move();
					if (System.currentTimeMillis() - timer > BALL_REFRESH_TIME) {
						timer = System.currentTimeMillis();
						board.dropBall();
					}
					Thread.sleep(delay);
				}
				gameStart = false;
			} catch (InterruptedException e) {}
		}
		
	}
	
	private void updateScore() {
		String str = sText.getText();
		sText.replaceRange(" " + score, str.indexOf(": ") + 1, str.length());
	}
	
	class GameBoard extends JPanel {
		private Door headDoor, tailDoor, centralDoor;
		private Ball ball;
		private boolean alive = true;
		private Random rand = new Random();
		private JTextArea inst;
		
		public GameBoard() {
			init();
			addKeyListener(keyListener);
		}
		
		public GameBoard(int x) {
			welcome();
		}
		
		public void init() {
			super.paintComponent(super.getGraphics());
			remove(inst);
			alive = true;
			ball = new Ball();
			headDoor = new Door();
			tailDoor = headDoor;
			centralDoor = headDoor;
			for (int i = 0; i < 10; i++) addDoor();
		}
		
		private void welcome() {
			inst = new JTextArea();
			for (int i = 0; i < 7; i++) inst.append("************************\n");
			inst.append("************************  Simple Java version of Flappy Bird\n");
			inst.append("************************  by Yongqiang Chen @ University of Minnesota\n");
			inst.append("************************  chen1502@umn.edu\n");
			inst.append("************************\n");
			inst.append("************************  Instructions: \n");
			inst.append("**********************  Use game menu or hotkeys \"E\"(Easy), \"M\"(Medium), \"H\"(Hard) to choose a level to start\n");
			inst.append("**********************  Use \"Up\" or \"Down\" to control the ball(bird)\n");
			for (int i = 0; i < 7; i++) inst.append("************************\n");
			add(inst, BorderLayout.CENTER);
			inst.setFocusable(false);
			inst.setOpaque(false);
			inst.setFont(new Font("Dialog", Font.PLAIN, 24));
		}
		
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(DOOR_COLOR);
			Door d = headDoor;
			while (d != null) {
				g2.fill(d.getArea());
				d = d.next;
			}
			
			if (ball != null) {
				g2.setColor(BALL_COLOR);
				g2.fill(ball.getArea());
			}
		}
		
		public boolean isAlive() { return alive; }
		public void setAlive() { alive = true; }
		
		public void move() {
			Door d = headDoor;
			while(d != null) {
				d.moveLeft();
				if(d.touch(ball)) alive = false;
				d = d.next;
			}
			if (centralDoor.isPassBall()) {
				score++;
				updateScore();
				centralDoor = centralDoor.next;
			}
			if (headDoor.getX() < - 2 * DOOR_WIDTH1) {
				headDoor = headDoor.next;
				addDoor();
			}
			
			repaint();
		}
		
		
		
		public boolean dropBall() {
			return moveBall(ballDrop);
		}
		
		public boolean jumpBall(String keyText) {
			int d = 0;
			if (keyText.equals("Up")) d = -BALL_JUMP;
			else if (keyText.equals("Down")) d = BALL_JUMP;
			return moveBall(d);
		}
		
		public boolean moveBall(int d) {
			int y = ball.getY() + d;
			int yMax = (int)getBounds().getMaxY() - BALL_RADIUS;
			if ( y >= yMax || y <= BALL_RADIUS ){
				alive = false;
				return false;
			}
			ball.setY(y);
			return true;
		}
		
		
		/**
		 * the following functions may be modified to get more appropriate new door
		 * @return
		 */
		private void addDoor() {
			int x = tailDoor.getX(), y = tailDoor.getY();
			x = calX(x);
			y = calY(y);
			Door d = new Door(x, y);
			tailDoor.next = d;
			d.prev = tailDoor;
			tailDoor = d;
		}
		
		private int calX(int x) {
			return x + DOOR_H_BLANK - paramRandBlank + rand.nextInt(paramRandBlank);
		}
		
		
		private double mu() {
			return 50.0 - 2000.0 / (50.0 + score); 
		}
		
		private int calY(int y) {
			double p = rand.nextGaussian();
			int sign = (p > 0) ? 1 : -1;

			p = (p + 3) * mu();
			int pInt = (int) p;
			y += pInt * sign;
			if (y < DOOR_BDD) return DOOR_BDD;
			else if( y > FRAME_HEIGHT - DOOR_BDD - 60) return FRAME_HEIGHT - DOOR_BDD - 60;
			else return y;
		}
		
	}
	
	class Door {
	    private int x = FRAME_WIDTH + 100, y = FRAME_HEIGHT / 2;
	    public Door next = null, prev = null;
	    private boolean passedBall = false;
	    
	    public Door() {  }
	    public Door(int x, int y){
	    	this.x = x;
	    	this.y = y;
	    }
	    public int getX() { return x; }
	    public int getY() { return y; }
	    
	    public Area getArea() {
	    	Area area = new Area(new Rectangle2D.Double(x - DOOR_WIDTH1, y - DOOR_HEIGHT1 - DOOR_V_BLANK, 2 * DOOR_WIDTH1, DOOR_HEIGHT1));
	    	area.add(new Area(new Rectangle2D.Double(x - DOOR_WIDTH2, y - 1000 - DOOR_V_BLANK, 2 * DOOR_WIDTH2, 1000)));
	    	area.add(new Area(new Rectangle2D.Double(x - DOOR_WIDTH1, y + DOOR_V_BLANK, 2 * DOOR_WIDTH1, DOOR_HEIGHT1)));
	    	area.add(new Area(new Rectangle2D.Double(x - DOOR_WIDTH2, y + DOOR_V_BLANK, 2 * DOOR_WIDTH2, 1000)));
	    	
	    	return area;
	    }
	    
	    public void moveLeft() {
	    	x -= doorMove;
	    	if ( x < BALL_CENTER_X - 50) passedBall = true;
	    }
	    
	    public boolean touch(Ball ball) {
	    	int bx = BALL_CENTER_X;
			int by = ball.getY();
			
			if (bx + BALL_RADIUS + DOOR_WIDTH1 < x) return false;
			
			if (Math.abs(by - y) > DOOR_V_BLANK - BALL_RADIUS && 
	    			Math.abs(bx - x) < BALL_RADIUS + DOOR_WIDTH2) return true;
	    	
	        if (Math.abs(bx - x) < BALL_RADIUS + DOOR_WIDTH1 && 
	        		Math.abs(by - y) > DOOR_V_BLANK - BALL_RADIUS &&
	        		Math.abs(by - y) < DOOR_V_BLANK + DOOR_HEIGHT1 + BALL_RADIUS) return true;
	        
	        return false;
	    }
	    
	    public boolean isPassBall() { return passedBall; }
	    
	}
	
	private class Ball {
	    private int y = FRAME_HEIGHT / 2;
        
	    public Area getArea() {
	    	Ellipse2D ball = new Ellipse2D.Double();
	    	ball.setFrameFromCenter(BALL_CENTER_X, y, BALL_CENTER_X + BALL_RADIUS, y + BALL_RADIUS);
	    	return new Area(ball);
	    }	    
	        
	    public int getY() { return y; }
	    
	    public void setY(int y) {
	    	this.y = y;
	    }   
	}	
	
	
	private KeyAdapter keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			String keyText = KeyEvent.getKeyText(e.getKeyCode());
			//System.out.println("..." + keyText);
			if(keyText.equals("X")) System.exit(0);
			if (!gameStart) {
				if (keyText.equals("E")) {
					setEasy();
				} else if (keyText.equals("M")) {
					setMedium();
				} else if (keyText.equals("H")) {
					setHard();
				} 
				return;
			}
			gameStart = board.jumpBall(keyText);
		}
	};
	
	
	private final int FRAME_WIDTH = 1240;
	private final int FRAME_HEIGHT = 760;
	
	
	private final int BALL_JUMP = 60;
	private final int BALL_RADIUS = 10;
	private final int BALL_CENTER_X = FRAME_WIDTH / 2;  
	private final Color BALL_COLOR = new Color(1, 11, 111);
	private final long BALL_REFRESH_TIME = 30;
	
	
	
	private final int DOOR_WIDTH1 = 35;
	private final int DOOR_WIDTH2 = 20;
	private final int DOOR_HEIGHT1 = 20;
	private final int DOOR_V_BLANK = 75;
	private final int DOOR_H_BLANK = 200;
	private final Color DOOR_COLOR = Color.GREEN;
	private final int DOOR_BDD = 130;
	
	private int paramRandBlank = 10;
	private int doorMove = 1;
    private long delay = 7;
	private int ballDrop = 2;
}
