package inetintelliprocess.searchengine.socket;
/**
 * 服务器端
 */
import inetintelliprocess.entry.SearcherEntry;
import inetintelliprocess.util.LogWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 服务器端线程
 * @author
 *
 */
public class ServerThread extends Thread {
    private Socket socket;
    private InputStream clientInput;
    private OutputStream clientOutput;

    public ServerThread(Socket socket) {
        this.socket = socket;
        try {
            this.clientInput = socket.getInputStream();
            this.clientOutput = socket.getOutputStream();
        } catch (IOException e) {
            LogWriter.logger.error(e);
            e.printStackTrace();
        }
        start();//启动服务器端线程
    }

    @Override
    /**
     * 接收状态
     * @state
     * AlarmRequest和UserRequest
     * AlarmRequest只有启动功能，UserRequest包括启动和中止
     * 未开始0，正在1，中止2，已完成3.
     */
    public void run() {
        BufferedReader breader = new BufferedReader(new InputStreamReader(
                clientInput));
        try {
            String strLine = breader.readLine();
            String[] strList = null;
            strList = strLine.split(",");
            if("AlarmRequest".equalsIgnoreCase(strList[0])){
                System.out.println("ID:" + strList[1]+"空格");
                SearcherEntry.getInstance().recieveAlarmID(strList[1]);
                PrintWriter printWriter = new PrintWriter(clientOutput, true);
                printWriter.flush();
                printWriter.println("Recieve Alarm Task");

                System.out.println("Finish sending to client...");
            }else if("UserRequest".equalsIgnoreCase(strList[0])){
                System.out.println("ID:" + strList[2]+"空格");
                if("1".equalsIgnoreCase(strList[1])){
                    SearcherEntry.getInstance().recieveUserID(strList[2],strList[3]);
                    PrintWriter printWriter = new PrintWriter(clientOutput, true);
                    printWriter.flush();
                    printWriter.println("Recieve User Start Task");
                    System.out.println("Finish sending to client...");
                }else if("2".equalsIgnoreCase(strList[1])){
                    SearcherEntry.getInstance().cancelUserID(strList[2]);
                    PrintWriter printWriter = new PrintWriter(clientOutput, true);
                    printWriter.flush();
                    printWriter.println("Recieve User Suspend Task");
                    System.out.println("Finish sending to client...");
                }
            }else {
                PrintWriter printWriter = new PrintWriter(clientOutput, true);
                printWriter.println("over");
            }
            clientInput.close();
            clientOutput.close();
            socket.close();

        } catch (IOException e) {
        }

    }
}
