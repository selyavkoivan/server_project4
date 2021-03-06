package server;


import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import server.Consts.Commands;
import server.Consts.Role;
import server.Database.*;
import server.FactoryGson.GsonDateFormatGetter;
import server.FactoryGson.GsonGetter;
import server.Models.Admin;
import server.Models.User;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
 class Port{
     private int port;
}
public class Main {
    public static void main(String[] args) {
        new Main().createServer();
    }



    Socket _socket;
    ServerSocket _ssocket;

    private void createServer() {

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader("package.json"));
            Port data = gson.fromJson(reader, Port.class);
            _socket = new Socket();
            _ssocket = new ServerSocket(data.getPort());
            System.out.println("Сервер запущен : " + _ssocket.getLocalSocketAddress());
            while (true) {

                _socket = _ssocket.accept();
                System.out.println(23);
                new Server(_socket);
            }

        } catch (IOException ioex) {

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ioex);
        }
    }


    class Server implements Runnable {
        private Socket clientSocket;

        public Server(Socket socket) {
            super();
            clientSocket = socket;
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                System.out.println("Подключен новый пользователь : " + clientSocket.getInetAddress());
                Menu();
                System.out.println("Пользователь отключен : " + clientSocket.getInetAddress());
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void Menu() {
            try {


                while (true) {
                    try {
                        String[] message = splitMessage();
                        System.out.println(message[1]);
                        switch (message[0]) {
                            case Commands.SIGN_UP -> Server.Send(clientSocket, UserManager.getDatabaseManager().reg(message[1]));
                            case Commands.SIGN_IN -> {
                                User user = UserManager.getDatabaseManager().sign(message[1]);
                                if (user != null) {
                                    Admin admin = AdminManager.getDatabaseManager().getAdminData(user);
                                    if (admin != null) Server.Send(clientSocket, Role.ADMIN.toString() + admin);
                                    else Server.Send(clientSocket, Role.USER.toString() + user);
                                } else Server.Send(clientSocket, Role.ERROR.toString());
                            }
                            case Commands.SHOW_USERS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(UserManager.getDatabaseManager().showUsers()));
                            case Commands.EDIT_ADMIN -> Server.Send(clientSocket, AdminManager.getDatabaseManager().editAdmin(message[1]));
                            case Commands.SHOW_ADMIN -> Server.Send(clientSocket, AdminManager.getDatabaseManager().getAdminData(message[1]).toString());
                            case Commands.SET_NEW_ADMIN -> AdminManager.getDatabaseManager().SetNewAdmin(message[1]);
                            case Commands.SHOW_GOODS -> Server.Send(clientSocket, new GsonGetter().getGson().toJson(ProductManager.getDatabaseManager().ShowGoods()));
                            case Commands.EDIT_PRODUCT -> Server.Send(clientSocket, ProductManager.getDatabaseManager().editProduct(message[1]));
                            case Commands.ADD_PRODUCT -> ProductManager.getDatabaseManager().addProduct(message[1]);
                            case Commands.DELETE_PRODUCT -> ProductManager.getDatabaseManager().deleteProduct(message[1]);
                            case Commands.SHOW_ORDERS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(OrderManager.getDatabaseManager().showOrders()));
                            case Commands.SHOW_USER_ORDERS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(OrderManager.getDatabaseManager().showUserOrders(message[1])));
                            case Commands.ADD_ORDER -> Server.Send(clientSocket, OrderManager.getDatabaseManager().createOrder(message[1]));
                            case Commands.DELETE_ORDER -> OrderManager.getDatabaseManager().deleteOrder(message[1]);
                            case Commands.SHOW_PRODUCT -> Server.Send(clientSocket, ProductManager.getDatabaseManager().ShowProduct(message[1]).toString());
                            case Commands.ADD_CARD -> UserManager.getDatabaseManager().AddCard(message[1]);
                            case Commands.EDIT_CARD -> UserManager.getDatabaseManager().EditCard(message[1]);
                            case Commands.DELETE_CARD -> UserManager.getDatabaseManager().DeleteCard(message[1]);
                            case Commands.EDIT_USER -> Server.Send(clientSocket, UserManager.getDatabaseManager().editUser(message[1]));
                            case Commands.EDIT_USER_STATUS -> UserManager.getDatabaseManager().editUserStatus(message[1]);
                            case Commands.SHOW_USER -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(UserManager.getDatabaseManager().showUser(message[1])));
                            case Commands.FILTER_USERS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(UserManager.getDatabaseManager().showFilterUsers(message[1])));
                            case Commands.FILTER_GOODS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(ProductManager.getDatabaseManager().ShowFilterGoods(message[1])));
                            case Commands.FILTER_USER_ORDERS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(OrderManager.getDatabaseManager().showUserFilterOrders(message[1])));
                            case Commands.EDIT_DELIVERY_STATUS -> OrderManager.getDatabaseManager().editOrderStatus(message[1]);
                            case Commands.GET_PRODUCT_RATES -> Server.Send(clientSocket, new GsonGetter().getGson().toJson(ProductManager.getDatabaseManager().GetProductRates(message[1])));
                            case Commands.GET_RATES -> Server.Send(clientSocket, new GsonGetter().getGson().toJson(ProductManager.getDatabaseManager().GetRates()));
                            case Commands.SET_RATE -> ProductManager.getDatabaseManager().SetRate(message[1]);
                            case Commands.EDIT_USER_PASSWORD -> UserManager.getDatabaseManager().editUserPassword(message[1]);
                            case Commands.ADD_MESSAGE -> ChatManager.getDatabaseManager().AddMessage(message[1]);
                            case Commands.GET_ORDER_MESSAGE -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(ChatManager.getDatabaseManager().GetMessages(message[1])));
                            case Commands.FILTER_ORDERS -> Server.Send(clientSocket, new GsonDateFormatGetter().getGson().toJson(OrderManager.getDatabaseManager().showFilterOrders(message[1])));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();

                    }

                }
            } catch (IOException ex) {
            }
        }

        private String[] splitMessage() throws IOException {
            String message = Server.Recv(clientSocket);
            System.out.println(message);
            return new String[]{message.substring(0, 3), message.substring(3)};
        }

        public static void Send(Socket socket, String message) {
            try {
                var out = socket.getOutputStream();
                var messsageBuffer = message.getBytes();
                byte[] length = new byte[4];
                length[0] = (byte) (messsageBuffer.length & 0xff);
                length[1] = (byte) ((messsageBuffer.length >> 8) & 0xff);
                length[2] = (byte) ((messsageBuffer.length >> 16) & 0xff);
                length[3] = (byte) ((messsageBuffer.length >> 24) & 0xff);
                out.write(length);
                out.write(messsageBuffer, 0, messsageBuffer.length);
            } catch (IOException exception) {
                return;
            }
        }

        public static String Recv(Socket socket) throws IOException {
            var stream = socket.getInputStream();
            var sizeBuffer = new byte[4];
            stream.read(sizeBuffer, 0, 4);
            int size = (((sizeBuffer[3] & 0xff) << 24) | ((sizeBuffer[2] & 0xff) << 16) |
                    ((sizeBuffer[1] & 0xff) << 8) | (sizeBuffer[0] & 0xff));
            var messageBuffer = new byte[size];
            stream.read(messageBuffer, 0, size);
            return new String(messageBuffer, "UTF-8").trim();
        }
    }
}

