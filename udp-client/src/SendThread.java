import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


// Поток отвечает за отправку сообщений пользователя
public class SendThread extends Thread {

    // Конструктор получает клиентский сокет, номер порта и IP адрес сервера
    SendThread(DatagramSocket socket, int port, InetAddress ip) {
        this.socket = socket;
        this.port = port;
        this.ip = ip;
    }

    private InetAddress ip;         // IP адрес
    private DatagramPacket packet;  // Датаграмма
    private DatagramSocket socket;  // Сокет
    private int port;               // Порт
    private boolean isRunning;

    // Буферы для отправки сообщений
    private byte[] sendingDataBuffer = new byte[1024];
    String text = " ";


    // Тело потока
    public void run() {
        isRunning = true;
        System.out.println("Отправка сообщений началась");
        Scanner scanner = new Scanner(System.in);

        // Формирование и отправка сообщений серверу
        while (isRunning) {
            sendingDataBuffer = new byte[1024];
            text = scanner.nextLine();
            sendingDataBuffer = text.getBytes();
            packet = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, ip, port);
            try {
                socket.send(packet);
            } catch (IOException exp) {
                exp.printStackTrace();
            }
            if (text.startsWith("/end"))
                isRunning = false;

        }

        // Завершение потока после отправки "Bye"
        this.interrupt();
        System.out.println("Отправка сообщений завершена");
    }
}
