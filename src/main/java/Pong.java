
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.*;

public class Pong implements ActionListener, KeyListener, Runnable
{
	//public static Pong pong;
	public Pong pong;
	public Pong pong2;

	public int width = 700, height = 700;

	public Renderer renderer;

	public Paddle player1;

	public Paddle player2;

	public Ball ball;

	public boolean ready = false;

	public boolean bot = false, selectingDifficulty;

	public boolean w, s, up, down;

	public String UP = "no";

	public int gameStatus = 0, scoreLimit = 7, playerWon; //0 = Menu, 1 = Paused, 2 = Playing, 3 = Over

	public int botDifficulty, botMoves, botCooldown = 0;

	public Random random;

	public JFrame jframe;

	public Thread myThread;

	public int port = 3000;

	public Server server;

	public int numberPlayer = 1;

	public InetAddress ipAddress; //from string to ipaddress

	public  DatagramSocket dataSocket;

	public boolean newPlayer = false;

	public boolean frameVisible = true;

	public void run() {
		Timer timer = new Timer(20, this);
		random = new Random();

		jframe = new JFrame(String.format("Pong%s", port - 3001));

		renderer = new Renderer();

		jframe.setSize(width + 15, height + 35);
		jframe.setVisible(true);
		jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jframe.add(renderer);
		jframe.setFocusable(true);

		/*MouseListener base_click = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent) {

			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent) {

			}

			@Override
			public void mouseExited(MouseEvent mouseEvent) {
			}
		};
		jframe.addMouseListener(base_click); */
		jframe.addKeyListener(pong);

		timer.start();

	}

	public Pong(int number, int port, DatagramSocket dataSocket, Server server)
	{
		this.dataSocket = dataSocket;
		this.server = server;
		myThread = new Thread(this);
		numberPlayer = number;

		if (port % 2 == 0)
			port--;
		else
			port++;
		this.port = port;
		this.server = server;
		try {
			ipAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void go() {
		myThread.start();
	}

	public void close() {
		this.jframe.dispose();
	}

	public void start()	{
		int i = 0;
		player1 = new Paddle(1);
		player2 = new Paddle(2);
		gameStatus = 2;
	}

	public void update()
	{
		if (player1.score >= scoreLimit)
		{
			playerWon = 1;
			gameStatus = 3;
		}

		if (player2.score >= scoreLimit)
		{
			gameStatus = 3;
			playerWon = 2;
		}
		if (bot) {
			if (w) {

				player1.move("up");
			}
			if (s) {
				player1.move("do");
			}
			if (botCooldown > 0) {
				botCooldown--;

				if (botCooldown == 0) {
					botMoves = 0;
				}
			}

			if (botMoves < 10) {
				if (player2.y + player2.height / 2 < ball.y) {
					player2.move("do");
					botMoves++;
				}

				if (player2.y + player2.height / 2 > ball.y) {
					player2.move("up");
					botMoves++;
				}

				if (botDifficulty == 0) {
					botCooldown = 20;
				}
				if (botDifficulty == 1) {
					botCooldown = 15;
				}
				if (botDifficulty == 2) {
					botCooldown = 10;
				}
			}
		}
		else {
			if (port % 2 != 0) { //player1 играем за левого
				if (w) {
					player1.move("up");
					send("up");
				}
				if (s) {
					player1.move("do");
					send("do");
				}

				SendRecive recive = new SendRecive(1, pong);
				recive.action = 1;
				recive.start();
				if (!UP.equals("no"))
				    player2.move(UP);
				UP = "no";
			}
			else {
				if (up) {
					player2.move("up");
					send("up");
				}
				if (down) {
					player2.move("do");
					send("do");
				}

				SendRecive recive = new SendRecive(1, pong);
				recive.action = 1;
				recive.start();
				if (!UP.equals("no"))
				    player1.move(UP);
				UP = "no";
			}
		}
		ball.update(player1, player2);
		if (pong2 != null) {
            pong2.player1.score = player1.score;
            pong2.player2.score = player2.score;
        }

	}

	private class SendRecive extends Thread {
		public int action; //0 - send, 1 - recive


		public SendRecive (int a, Pong pong1) {
			action = a;
		}
		public void run() {

			//if (action != numberPlayer) {
            while (true) {
                try {
                    byte[] buf = new byte[2];
                    DatagramPacket pac = new DatagramPacket(buf, buf.length);
                    dataSocket.receive(pac);
                    String line = new String(pac.getData(), 0, pac.getLength());
                    if (line.equals("do"))
                        UP = "do";
                    else if (line.equals("up"))
                        UP = "up";
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error with datagramsocket, see class Client.send()");
                }
            }

		}
	}

	public void send(String Up) {
		try {
			String data = Up;
			if(Up.equals("cl"))
				gameStatus = 0;
			byte[] buf = data.getBytes();

			DatagramPacket pac = new DatagramPacket(buf, buf.length, ipAddress, this.port);
			dataSocket.send(pac);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error with datagramsocket, see class Client.send()");
		}
	}

	public void render(Graphics2D g)
	{
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (gameStatus == 0)
		{
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", 1, 50));

			g.drawString("PONG", width / 2 - 75, 50);

			if (!selectingDifficulty)
			{
				g.setFont(new Font("Arial", 1, 30));

				g.drawString("Press Space to Play", width / 2 - 150, height / 2 - 25);
				g.drawString("Press Shift to Play with Bot", width / 2 - 200, height / 2 + 25);
			//	g.drawString("<< Score Limit: " + scoreLimit + " >>", width / 2 - 150, height / 2 + 75);
			}
		}

		if (gameStatus == 1)
		{
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", 1, 50));
			g.drawString("PAUSED", width / 2 - 103, height / 2 - 25);
		}

		if (gameStatus == 1 || gameStatus == 2)
		{
			g.setColor(Color.WHITE);

			g.setStroke(new BasicStroke(5f));

			g.drawLine(width / 2, 0, width / 2, height);

			g.setStroke(new BasicStroke(2f));

			g.drawOval(width / 2 - 150, height / 2 - 150, 300, 300);

			g.setFont(new Font("Arial", 1, 50));

			g.drawString(String.valueOf(player1.score), width / 2 - 90, 50);
			g.drawString(String.valueOf(player2.score), width / 2 + 65, 50);

			player1.render(g);
			player2.render(g);
			ball.render(g);
			//if (pong2 !=null)
			//    pong2.ball.render(g);

		}

		if (gameStatus == 3)
		{
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", 1, 50));

			g.drawString("PONG", width / 2 - 75, 50);

			if (bot && playerWon == 2)
			{
				g.drawString("The Bot Wins!", width / 2 - 170, 200);
			}
			else {
				g.drawString("Player " + playerWon + " Wins!", width / 2 - 165, 200);
			}

			g.setFont(new Font("Arial", 1, 30));

			//g.drawString("Press Space to Play Again", width / 2 - 185, height / 2 - 25);
			g.drawString("Press ESC for Menu", width / 2 - 140, height / 2 + 25);
		}
		if (gameStatus == 4) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", 1, 50));
            g.drawString("WAIT ", width / 2 - 103, height / 2 - 25);
        }
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        //System.out.println("GAME STATUS = 0 " + pong);
        if (gameStatus == 2) {
            //System.out.println("GAME STATUS = 2 " + pong);
			update();
		}
        renderer.repaint();
	}

	/*public static void main(String[] args)
	{
		pong = new Pong();
	}*/

	@Override
	public void keyPressed(KeyEvent e)
	{
		int id = e.getKeyCode();

		if (id == KeyEvent.VK_W) {
			w = true;
		}
		else if (id == KeyEvent.VK_S)
		{
			s = true;
		}
		else if (id == KeyEvent.VK_UP)
		{
			up = true;
		}
		else if (id == KeyEvent.VK_DOWN)
		{
			down = true;
		}
		else if (id == KeyEvent.VK_RIGHT)
		{
			if (selectingDifficulty)
			{
				if (botDifficulty < 2)
				{
					botDifficulty++;
				}
				else
				{
					botDifficulty = 0;
				}
			}
		}
		else if (id == KeyEvent.VK_ESCAPE && (gameStatus == 2 || gameStatus == 3)) {
			gameStatus = 0;
			if (pong2 != null)
			    pong2.gameStatus = 0;
		}
		else if (id == KeyEvent.VK_SHIFT) {
			bot = true;
			ball.speed = 4;
            start();
		}
		else if (id == KeyEvent.VK_SPACE) {
            pong.ready = true;
			if (newPlayer) {
			    if (pong2.ready) {
                    if (gameStatus == 0 || gameStatus == 3) {
                        if (!selectingDifficulty) {
                            bot = false;
                        } else {
                            selectingDifficulty = false;
                        }
                        start();
                        pong2.start();
                    } else if (gameStatus == 1) {
                        gameStatus = 2;
                        pong2.gameStatus = 2;
                    } else if (gameStatus == 2) {
                        gameStatus = 1;
                        pong2.gameStatus = 1;
                    }
                }
                else
                    gameStatus = 4;
			}
			else
			    gameStatus = 4;
		}
	}
	public void setNewPlayer(boolean newPlayer) {
		this.newPlayer = newPlayer;
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		int id = e.getKeyCode();

		if (id == KeyEvent.VK_W)
		{
			w = false;
		}
		else if (id == KeyEvent.VK_S)
		{
			s = false;
		}
		else if (id == KeyEvent.VK_UP)
		{
			up = false;
		}
		else if (id == KeyEvent.VK_DOWN)
		{
			down = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{

	}

	public class Renderer extends JPanel
	{

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			render((Graphics2D) g);
		}

	}




}
