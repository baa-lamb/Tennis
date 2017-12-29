
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

public class Server {

    private DatagramSocket dataSocket;

    private InetAddress ipAddress; //from string to ipaddress;

    private int countUser = 0; // колличество юзеров

    private int currentPort = 3000;

    private Client client;

    public Map games = new HashMap<Integer, Pong>();
    public ArrayList<Pong> pongs = new ArrayList<Pong>();
    private ArrayList<Client> allUsers = new ArrayList<Client>();
    private ArrayList<Integer> numberUsers = new ArrayList<Integer>();

    public Server() {
        try {
            dataSocket = new DatagramSocket();
            ipAddress = InetAddress.getByName("127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in constructor, see class Server");
        }
    }

    public void newGame() {
        countUser++;
        currentPort++;
        Ball ball = new Ball();
        Client c = new Client(countUser, currentPort, this, ball);
        c.startGame();
        c.getPong().ball = ball;
        games.put(currentPort, c.p.pong);
        if (countUser % 2 == 0) {
            c.p.setNewPlayer(true);
            Pong pon = (Pong)games.get(currentPort - 1);
            pon.setNewPlayer(true);
            c.getPong().pong2 = pon;
            c.getPong().ball = ball;
            pon.ball = ball;
            pon.pong2 = c.getPong();
            games.put(currentPort - 1, pon);
        }

       /* int i;
        for (i = 0; i < numberUsers.size(); i++) {
            if (numberUsers.get(i) == 0) {
                numberUsers.set(i, 1);
                break;
            }
        }
        if (i != numberUsers.size()) { //если в списке есть свободное место(один пользователь вышел, а другой играть хочт)
            currentPort = allUsers.get(i).port;
            allUsers.set(i, u);
            countUser = i;
        }
        else { //если дошли до конца списка и свободных позиций нет
            //currentPort++;
            allUsers.add(u);
            numberUsers.add(1);
            countUser++;
        }*/

        //pongs.add(u.getPong());
    }

    public void close() {
        try {
            dataSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < allUsers.size(); i++) {
            deleteUser(i);
        }
    }

    public void deleteUser(int number) {
        numberUsers.set(number, 0);
        allUsers.get(number).close();
    }

    public class Client extends Thread {
        private Server server;

        private DatagramSocket dataSocket;

        private InetAddress ipAddress;

        private boolean up = true;

        public int port = 3000;

        public int numberClient = 0; //номер клиента, вычислется при создании новой игры. равен индексу в массиве

        public Pong p;

        public Client(int number, int port, Server server, Ball ball) {
            this.server = server;
            try {
                this.port = port;
                System.out.println(port);
                numberClient = number++;
                ipAddress = InetAddress.getByName("127.0.0.1"); //from string to ipaddress
                dataSocket = new DatagramSocket(this.port);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error with IP, see class Client");
            }

            p = new Pong(numberClient, port, dataSocket, server); // запуск игры
            p.pong = p;
           // p.pong.player1 = pad1;
           // p.pong.player2 = pad2;
          /// p.pong.ball = ball;
        }

        public void startGame() {
            p.go();
        }

        public void close() {
            if (p != null) {
                p.close();
            }
            dataSocket.close();
        }

        public Pong getPong() {
            return p;
        }
    }

}

/*
*     private class User extends Thread {
        private DatagramSocket socket;
        private Server serv;
        private int port;
        private Client c;
        private Client c1;
        private int num;

        public User (int number, int port, Server serv) {
            this.port = port;
            this.serv = serv;
            this.num = number;
        }

        public void go() {
            c =  new Client(num, port, this.serv); // запуск игры
            if(num > 0) {
                //c1 = new Client(num++, port, this.serv); // запуск игры
                //c1.startGame();
            }
            c.startGame();
        }

        public void close() {
            c.p.pong.close();
            c.close();
            if (c1 != null) {
                c1.p.pong.close();
                c.close();
            }
        }

        public Pong getPong() {
            return c.p.pong;
        }
    }
* */
