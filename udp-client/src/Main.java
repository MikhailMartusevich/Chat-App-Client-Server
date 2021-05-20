import java.net.*;

public class Main {

    public static void main(String[] args) {

        final int port = 8888;  // Определение используемого порта

        try {
            // Создание сокета и определение IP адреса сервера
            DatagramSocket socket = new DatagramSocket(port);
            InetAddress ip = InetAddress.
                    getLocalHost();
                    //getByName(args[0]);

            Thread send_thread = new SendThread(socket, port, ip);
                send_thread.start();

            Thread recv_thread = new ReceiveThread(socket);
                recv_thread.start();

                // Ожидание закрытия потока отправления
                while(!send_thread.isInterrupted()) {}
                recv_thread.interrupt(); // закрытие потока принятия

            System.out.println("All threads are interrupted");
        }
        catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
