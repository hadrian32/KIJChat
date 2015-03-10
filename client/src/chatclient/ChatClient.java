package chatclient;


import java.net.*;
import java.io.*;
import java.util.*;
/**
 * @hadrianbsrg
 */

public class ChatClient extends javax.swing.JFrame  {
    String username, password, serverIP = "192.168.1.111";
    int Port = 8888;
    Socket sock;
    BufferedReader reader;
    PrintWriter writer;
    BufferedWriter writer2;
    ArrayList<String> userList = new ArrayList();
    Boolean isConnected = false;

    public ChatClient() {
        initComponents();
    }
    
    //handler paket masuk.
    public class IncomingReader implements Runnable{ 
        
        public void run() {
            
            String[] data;
            String stream, done = "Done", connect = "Connect", disconnect = "Disconnect", chat = "Chat", online = "online";
            String message = "message", okay = "ok";
            

            try {
                while ((stream = reader.readLine()) != null) {

                    data = stream.split(" ");
                   
                     if (data[0].equals(message)) {

                        chatTextArea.append(data[1] + ": " + data[4] + "\n");
                        chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());

                    }
                     else if (data[0].equals(online)){

                        chatTextArea.removeAll();
                        int counter = data.length;
                        while (counter != 0){
                            userAdd(data[counter]);  
                            counter--;
                        }                   
                     }
                     
                     else if (data[2].equals(disconnect)) {

                        userRemove(data[0]);

                    } 
                     else if (data[2].equals(done)) {

                        usersList.setText("");
                        writeUsers();
                        userList.clear();
                    }
                     else if (data[1].equals(okay)){
                         okayLogin();
                     }
                }
           }catch(Exception ex) {
           }
        }
    }
    
    public class KirimPing implements Runnable{
       public void run(){
        String pingText = "ping " + username;
        writer.println(pingText);
        writer.flush();
        writer.flush();
        writer.flush();
       }
    }
    
    
    //thread buat IncomingReader -> handler paket masuk
    public void ListenThread() {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
    
    public void PingThread(){
        Thread KirimPing = new Thread(new KirimPing());
        KirimPing.start();
    }

    public void userAdd(String data) {
         userList.add(data);
     }

    public void userRemove(String data) {
         chatTextArea.append(data + " has disconnected.\n");
     }

    public void writeUsers() {
         String[] tempList = new String[(userList.size())];
         userList.toArray(tempList);
         for (String token:tempList) {

             usersList.append(token + "\n");
         }
     }
    
    public void sendPing(){
        while(true){
        String pingText = "ping " + username;
        writer.println(pingText);
        writer.flush();
        writer.flush();
        writer.flush();
        }
    }
        
    
    
    public String okayLogin(){
        String berhasil = "login ok";
        return berhasil;
    }
    
    
    /* DISCONNECT FUNCTION */
    public void sendDisconnect() {

       String bye = (username + ": :Disconnect");
        try{
            writer.println(bye); // Sends server the disconnect signal.
            writer.flush(); // flushes the buffer
        } catch (Exception e) {
            chatTextArea.append("Could not send Disconnect message.\n");
        }
      }

    public void Disconnect() {

        try {
               chatTextArea.append("Disconnected.\n");
               sock.close();
        } catch(Exception ex) {
               chatTextArea.append("Failed to disconnect. \n");
        }
        isConnected = false;
        usernameField.setEditable(true);
        usersList.setText("");

      }
    /* END OF DISCONNECT FUNCTION */
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    @SuppressWarnings("unchecked")
    
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        inputTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        chatTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
    //  passwordField = new javax.swing.JTextField();
        connectButton = new javax.swing.JButton();
        disconnectButton = new javax.swing.JButton();
        sendButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        usersList = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Client");

        inputTextArea.setColumns(20);
        inputTextArea.setLineWrap(true);
        inputTextArea.setRows(5);
        jScrollPane1.setViewportView(inputTextArea);

        chatTextArea.setColumns(20);
        chatTextArea.setEditable(false);
        chatTextArea.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        chatTextArea.setLineWrap(true);
        chatTextArea.setRows(5);
        jScrollPane2.setViewportView(chatTextArea);

        jLabel1.setText("Login:");

        usernameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameFieldActionPerformed(evt);
            }
        });
        /*
        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });
        */

        connectButton.setText("Connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        disconnectButton.setText("Disconnect");
        disconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectButtonActionPerformed(evt);
            }
        });

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        usersList.setEditable(false);
        usersList.setColumns(20);
        usersList.setRows(5);
        jScrollPane3.setViewportView(usersList);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Online Users");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    //  .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectButton)
                        .addGap(18, 18, 18)
                        .addComponent(disconnectButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(disconnectButton)
                    .addComponent(connectButton)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                //  .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                        

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              

            if (isConnected == false) {
            username = usernameField.getText();
            //password = passwordField.getText();
            usernameField.setEditable(false);
            //passwordField.setEditable(false);
            try {
                sock = new Socket(serverIP, Port);
                InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(streamreader);
                writer = new PrintWriter(sock.getOutputStream());
                String kirim = "login " + username;                
                writer.println(kirim); //sends
                writer.flush(); // flushes the buffer
                String responLogin = okayLogin();
                if (responLogin.equals("login ok")){
                      isConnected = true; // connected true
                      chatTextArea.append("Connected!\n");
                }
            }
            catch (Exception ex) {
                chatTextArea.append("Cannot Connect \n");
                usernameField.setEditable(true);
                //passwordField.setEditable(true);
            }
            ListenThread();
            PingThread();
        } else if (isConnected == true) {
            chatTextArea.append("You are already connected. \n");
        }
    }                                             

    private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        sendDisconnect();
        Disconnect();
    }                                                

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        String nothing = "";
        String to = usersList.getSelectedText();
        if ((inputTextArea.getText()).equals(nothing)) {
            inputTextArea.setText("");
            inputTextArea.requestFocus();
        } else {
            try {
               writer.println("message " + username + " " + to + " " + "id " + inputTextArea.getText() + "\0"); //send to server
               writer.flush(); // flushes the buffer
            } catch (Exception ex) {
                chatTextArea.append("Message was not sent. \n");
            }
            inputTextArea.setText("");
            inputTextArea.requestFocus();
        }

        inputTextArea.setText("");
        inputTextArea.requestFocus();
    }                                          

    private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // TODO add your handling code here:
    }       
    
    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // TODO add your handling code here:
    }


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChatClient().setVisible(true);
            }
        });
    }            
    private javax.swing.JTextArea chatTextArea;
    private javax.swing.JButton connectButton;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JTextArea inputTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField usernameField;
  //private javax.swing.JTextField passwordField;
    private javax.swing.JTextArea usersList;              

}
