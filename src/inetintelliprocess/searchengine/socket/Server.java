package inetintelliprocess.searchengine.socket;
/**
 * 服务器端
 */
import inetintelliprocess.util.LogWriter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * server服务器端
 * 侦听来自客户端的请求
 *
 */
public class Server extends Thread{
    /**
     * 启动
     */
    public void run() {
        try {
            System.out.println("开始侦听请求....");
            ServerSocket serverSocket = new ServerSocket(2602);// 服务器套接字
            Socket clientSocket = null;
            while (true) {
                clientSocket = serverSocket.accept();// 获得客户端的请求的Socket
                System.out.println("已侦听到了客户端的请求....");
                new ServerThread(clientSocket);//一个Socket请求new一个线程来处理
            }
        } catch (IOException e) {
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
    }
}
