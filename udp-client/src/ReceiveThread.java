import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


// Поток отвечает за принятие сообщений от сервера
public class ReceiveThread extends Thread {

    // Конструктор получает клиентский сокет, номер порта и IP адрес сервера
    ReceiveThread(DatagramSocket socket) {
        this.socket = socket;
    }

    // Переназначение метода interrupt() класса Thread
    @Override
    public void interrupt() {
        socket.close();
        super.interrupt(); // Выполнение оригинального метода Thread.interrupt()
    }

    private DatagramPacket packet;     // Датаграмма
    private DatagramSocket socket;     // Сокет

    // Буферы для принятия сообщений
    private byte[] receivingDataBuffer = new byte[256];
    private String text;


    // Тело потока
    public void run() {

        System.out.println("Приём сообщений начался");
        while(!socket.isClosed()) {
            receivingDataBuffer = new byte[256];

            // Принятие сообщений от сервера
            packet = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
            try {
                socket.receive(packet);
            }
            catch(IOException exp) {
                exp.printStackTrace();
            }

            // Вывод сообщения на экран
            text = new String(packet.getData());

            // Завершение потока при входящем сообщении "Logout"
            if(text.startsWith("Server has ended.")) {
//                socket.close();
                System.out.println("Принятие сообщений завершилось");
                System.out.println("Вы вышли из чата");
                this.interrupt();
            }

            System.out.println(text);
        }
    }
}
