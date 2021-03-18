import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class Server {
    String srvName;
    String IP;
}

public class v3Client {
    static int TCPport = 9999;
    static int UDPport = 12346;
    static int UdestPort = 9998;
    DatagramSocket udpSocket;
    Socket clientSocket;
    String com = "";// which command the user require
    DataInputStream in;
    DataOutputStream out;
    ArrayList<Server> srvList = new ArrayList<Server>();

    // start client side
    public v3Client(String[] args) {
        // udp thread

        Thread udp = new Thread(() -> {
            try {
                discovery(args[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        udp.start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        try {
            new v3Client(srvList, TCPport);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Send broadcast to discover servers and receive message from servers through
    // udp port.

    public void discovery(String device) throws IOException {
        udpSocket = new DatagramSocket(UDPport);
        device = "PC " + device;
        byte[] str = device.getBytes();
        InetAddress destination = InetAddress.getByName("255.255.255.255");
        DatagramPacket packet = new DatagramPacket(str, str.length, destination, UdestPort);
        udpSocket.send(packet);
        getSrvList(packet);
    }

    // After receiving a message, add the server to the list which stores server
    // name and address.
    public void getSrvList(DatagramPacket packet) throws IOException {
        while (true) {
            DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
            udpSocket.receive(p);
            if (packet != p) {
                byte[] data = p.getData();
                String srvInfo = new String(data, 0, p.getLength());
                int size = p.getLength();
                String srcAddr = p.getAddress().toString();
                srcAddr = srcAddr.substring(1, srcAddr.length());
                Server s = new Server();
                s.srvName = srvInfo;
                s.IP = srcAddr;
                srvList.add(0, s);
            }
        }
    }

    // Print out the available servers for user to choose. After the server choose
    // one server, connect to the sever for functions.
    // TCP
    public v3Client(ArrayList<Server> srvList2, int port) throws IOException {
        boolean login = false;
        boolean exit = false;
        while (!exit) {
            if (srvList2.size() > 0) {

                boolean opCorrect = false;
                int option = 0;
                for (int i = 0; i < srvList2.size(); i++) {
                    System.out.println(i + " " + srvList2.get(i).IP + " " + srvList2.get(i).srvName);
                }

                while (!opCorrect) {
                    System.out.print("Which server do you want to join? (please input the index):");
                    Scanner srvOption = new Scanner(System.in);
                    option = srvOption.nextInt();
                    if (option < srvList2.size() && option >= 0)
                        opCorrect = true;
                    else
                        System.out.println("Wrong input");
                }
                if (opCorrect) {
                    clientSocket = new Socket(srvList2.get(option).IP, port);
                    in = new DataInputStream(clientSocket.getInputStream());
                    out = new DataOutputStream(clientSocket.getOutputStream());

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        exit = appStart(login, exit);
                        if (exit) {
                            clientSocket.close();
                            return;
                        }
                    } catch (IOException ex) {
                        System.err.println("Connection dropped!");
                        System.exit(-1);
                    }
                }
            }
        }
    }

    // Use TCP to communicate with server. This class is a controller for different
    // commands and this class sends the commands to the server.
    public boolean appStart(boolean login, boolean exit) throws IOException {
        Scanner scanner = new Scanner(System.in);
        do {
            byte[] buffer = new byte[1024];
            System.out.print("Please input your username: ");
            String username = scanner.nextLine();
            System.out.print("Please input password: ");
            String password = scanner.nextLine();
            if (username.equalsIgnoreCase("exit") || password.equalsIgnoreCase("exit")) {
                exit = true;
                String str = "exit";
                out.writeInt(str.length());
                out.write(str.getBytes(), 0, str.length());

                int len = in.readInt();
                in.read(buffer, 0, len);
                String serMsg = new String(buffer, 0, len);
                System.out.println(serMsg);
                return exit;
            }

            String str = "login " + username + " " + password;
            out.writeInt(str.length());
            out.write(str.getBytes(), 0, str.length());

            int len = in.readInt();
            in.read(buffer, 0, len);
            String serMsg = new String(buffer, 0, len);
            System.out.println(serMsg);
            if (serMsg.equals("Login success")) {
                login = true;
                System.out.println();
                System.out.println("Available commands:");
                System.out.println();
                System.out.println("| rd directory_path | Remove sub-directory ");
                System.out.println("| dir path | Read file list in the path ");
                System.out.println("| md new_directory_path | Create new directory ");
                System.out.println("| del file_path | File Delete");
                System.out.println("| readDe file_path | Read details of the file ");
                System.out.println("| ren original_file_path new_filename | File rename");
                System.out.println("| upload | Upload the file");
                System.out.println("| download | download the file");
                System.out.println("| logout");
                System.out.println("| exit");
                System.out.println("| help");
                System.out.println();

                do {
                    System.out.print("Please input your command>");
                    Scanner newScanner = new Scanner(System.in);
                    String cmd = newScanner.nextLine();
                    String[] com = cmd.split("\\s+");
                    if (com.length == 1) {
                        if (com[0].equalsIgnoreCase("help")) {
                            System.out.println("Available commands:");
                            System.out.println();
                            System.out.println("| rd directory_path | Remove sub-directory ");
                            System.out.println("| dir path | Read file list in the path ");
                            System.out.println("| md new_directory_path | Create new directory ");
                            System.out.println("| del file_path | File Delete");
                            System.out.println("| readDe file_path | Read details of the file ");
                            System.out.println("| ren original_file_path new_filename | File rename");
                            System.out.println("| upload | Upload the file");
                            System.out.println("| download | download the file");
                            System.out.println("| logout");
                            System.out.println("| exit");
                            System.out.println("| help");

                        } else if (com[0].equalsIgnoreCase("dir")) {
                            out.writeInt(cmd.length());
                            out.write(cmd.getBytes(), 0, cmd.length());
                            int length = in.readInt();

                            while (length > 0) {
                                int blen = in.read(buffer, 0, buffer.length);
                                String ser = new String(buffer, 0, blen);
                                System.out.println(ser);
                                length -= blen;
                            }
                        } else if (com[0].equalsIgnoreCase("logout")) {
                            login = false;
                            out.writeInt(cmd.length());
                            out.write(cmd.getBytes(), 0, cmd.length());
                            int l = in.readInt();
                            in.read(buffer, 0, l);
                            String sMsg = new String(buffer, 0, l);
                            System.out.println(sMsg);
                            break;
                        } else if (com[0].equalsIgnoreCase("upload")) {// command
                            upload(cmd);
                            int length = in.readInt();

                            while (length > 0) {
                                int blen = in.read(buffer, 0, buffer.length);
                                String ser = new String(buffer, 0, blen);
                                System.out.println(ser);
                                length -= blen;
                            }
                        } else if (com[0].equalsIgnoreCase("download")) {

                            download(cmd);

                        } else if (com[0].equalsIgnoreCase("exit")) {
                            login = false;
                            exit = true;
                            out.writeInt(cmd.length());
                            out.write(cmd.getBytes(), 0, cmd.length());
                            int l = in.readInt();
                            in.read(buffer, 0, l);
                            String sMsg = new String(buffer, 0, l);
                            System.out.println(sMsg);
                            return exit;
                        } else {
                            System.out.println("Wrong command.");
                        }
                    } else {

                        out.writeInt(cmd.length());
                        out.write(cmd.getBytes(), 0, cmd.length());
                        int length = in.readInt();

                        while (length > 0) {
                            int blen = in.read(buffer, 0, buffer.length);
                            String ser = new String(buffer, 0, blen);
                            System.out.println(ser);
                            length -= blen;
                        }
                    }
                } while (login);
            }
        } while (!exit);
        return exit;
    }

    // Upload the file to the server. Ask the user to input specific path and
    // filename. When uploading a file, the file name can be changed
    public void upload(String cmd) throws IOException {
        Scanner upload = new Scanner(System.in);
        System.out.print("Please input the path and filename of the file you want to upload:");
        String uploadfile = upload.nextLine();

        boolean fileExist = false;
        do {
            File fileToUpload = new File(uploadfile);
            if (fileToUpload.exists()) {
                if (fileToUpload.isDirectory()) {
                    System.out.print("This is a directory.");
                } else {
                    fileExist = true;
                    break;
                }
            } else {
                System.out.println(uploadfile + " does not exist.");
            }
            System.out.print("Please input the path and filename of the file you want to upload:");
            uploadfile = upload.nextLine();

        } while (!fileExist);
        cmd += " " + uploadfile;
        System.out.print("Please input where to upload with filename:");
        String uploadpath = upload.nextLine();
        cmd += " " + uploadpath;
        out.writeInt(cmd.length());
        out.write(cmd.getBytes(), 0, cmd.length());

        String[] com = cmd.split("\\s+");
        try {
            byte[] buffer = new byte[1024];
            File file = new File(com[1]);
            FileInputStream inFile = new FileInputStream(file);

            long size = file.length();
            out.writeLong(size);

            while (size > 0) {
                int len = inFile.read(buffer, 0, buffer.length);
                out.write(buffer, 0, len);
                size -= len;
            }
        } catch (IOException e) {
            System.err.println("Transmission error.");
        }
    }

    // Ask the user to input specific file name and path to download the file.
    public void download(String cmd) throws IOException {
        Scanner download = new Scanner(System.in);
        System.out.print("Please input the file you wish to download (path and filename):");
        String downloadfile = download.nextLine();
        cmd = "download " + downloadfile;
        boolean downloadPathExist = false;
        String downloadpath = "";
        do {
            System.out.print("Please input where do you wish to download the file:");
            downloadpath = download.nextLine();
            File downloadp = new File(downloadpath);

            if (downloadp.exists()) {
                if (downloadp.isDirectory()) {
                    downloadPathExist = true;

                } else {
                    System.out.println("This is not a directory.");
                }
            } else {
                System.out.println(downloadpath + " does not exist.");
            }
        } while (!downloadPathExist);
        out.writeInt(cmd.length());
        out.write(cmd.getBytes(), 0, cmd.length());

        byte[] buffer = new byte[1024];
        int length = in.readInt();
        in.read(buffer, 0, length);
        // The length of the return message. If the file can be download, the length of
        // return message will be 0.
        if (length == 0) {
            try {
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                int nameLen = in.readInt();
                in.read(buffer, 0, nameLen);
                String name = new String(buffer, 0, nameLen);
                name = downloadpath + "/" + name;
                File newfile = new File(name);
                long size = in.readLong();
                FileOutputStream output = new FileOutputStream(newfile);
                while (size > 0) {
                    int len = in.read(buffer, 0, buffer.length);
                    output.write(buffer, 0, len);
                    size -= len;

                }
                System.out.println("Download finished.");
            } catch (IOException e) {
                System.err.println("unable to download file.");
            }
        } else {

            String ser = new String(buffer, 0, length);
            System.out.println(ser);

        }

    }

    public void end() {
        udpSocket.close();
        System.out.println("bye-bye");
    }

}