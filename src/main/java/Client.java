/*package Pong;

import java.io.*;
import java.net.*;

public class Client extends Thread {
    private Server server;

    private DatagramSocket dataSocket;

    private InetAddress ipAddress;

    private boolean up = true;

    public int port = 3000;

    public int numberClient = 0; //номер клиента, вычислется при создании новой игры. равен индексу в массиве

    public Pong p;

    public Client(int number, int port, Server server) {
        this.server = server;
        try {
            this.port = port;
            System.out.println(port);
            numberClient = number;
            ipAddress = InetAddress.getByName("127.0.0.1"); //from string to ipaddress
            dataSocket = new DatagramSocket(this.port);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error with IP, see class Client");
        }
    }

    public void startGame() {
        p = new Pong(numberClient, port, dataSocket, server); // запуск игры
        p.pong = p;
        p.go(p);
    }

    public void close() {
        if (p != null) {
            p.close();
        }
        dataSocket.close();
    }
}

    //на сервере  3 координаты - мяч, 2 игрока;
    //сервер просчитывает положение мяча и отправляет его пользователю
    //клиент определяет координаты платформы
    //сервер отсылаеют только координаты
    //
    //
*/