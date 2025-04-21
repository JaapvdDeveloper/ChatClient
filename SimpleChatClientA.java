import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleChatClientA {
    private JTextArea incoming;
    private JTextField outgoing;
    private PrintWriter writer;
    private BufferedReader reader;
    public void go() {
        //Method that will connect to the server
        setUpNetworking();
        JScrollPane scroller = createScrollableTextArea();
        //building the gui for the chat client
        outgoing = new JTextField(20);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel mainPanel = new JPanel();
        mainPanel.add(scroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);

        //New job, an inner class, which is a Runnable. The job is to read from the server's socket stream, displaying any incoming messages in the scrolling text area. We start this job using a single thread executor since we know we want to run only this one job.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new IncomingReader());

        JFrame frame = new JFrame("Simple Chat Client");
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 100);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // call the setUpNetworking() method
        // make gui and register a listener with the send button
    }

    private JScrollPane createScrollableTextArea() {
        incoming = new JTextArea(15, 30);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane scroller = new JScrollPane(incoming);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroller;
    }

    //We're using Channels to create a new reader and writer for the SocketChannel that's connected to the server.
    //THe writer sends messages to the server, and now we're using a reader so that the reader job can get messages from the server
    private void setUpNetworking() {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);
            // Open een socket channel that connects to the server
            SocketChannel socketChannel = SocketChannel.open(serverAddress);
            //This is where we make the PrintWriter from a writer that writes to the SocketChannel
            reader = new BufferedReader(Channels.newReader(socketChannel, UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, UTF_8));
            System.out.println("Networking established");

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Open a SocketChannel to the server
        // make a printwriter and assign to writer instance variable

    }
    //This is what the thread does!
    //In the run() method, it stays in a loop (as long as what it gets from the server is not null), reading a line at a time and adding each line to the scrolling text (along with a new line character)
    public class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("read " + message);
                    incoming.append(message + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //The writer is chained to the writer from the SocketChannel, so whenever we do a println() it goes over the network to the server.
    private void sendMessage() {
        writer.println(outgoing.getText());
        writer.flush();
        outgoing.setText("");
        outgoing.requestFocusInWindow();
        // get the text from the text field and
        // send it to the server using the writer

    }

    public static void main(String[] args) {
        new SimpleChatClientA().go();
    }

}
