package Client;

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;


/**
 * This is the GUI Client class. It automatically connected to the server using the socket.
 * When ran a nice GUI appears with buttons and labels that lets you play the game using coloured
 * pieces like the actual connect 4 game.
 *
 */
public class GUIClient {

    private JLabel messageLabel; //The JLabel that displays information to the player about the game.
    private JLabel[][] GUIBoard = new JLabel[6][7]; // 2D array "local" copy of the game board represented using the GUI JLabels.

    public GUIClient() {
        try (Socket conn = new Socket("localhost", 1024); PrintWriter sockOut = new PrintWriter(conn.getOutputStream(), true); BufferedReader sockIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));) {
            createGUI(sockOut); //Method called to create and initialize the GUI.

            /*
            This is loop constantly repeats to listen to what the server is outputting.
            Based on what the server sent, a series of if statements determine what to do next.
             */
            while (true) {
                String sockLine = sockIn.readLine();
                if (sockLine.startsWith("-") || sockLine.startsWith("|")) { //If the server sent the updated game board...
                    updateBoard(sockIn); //Update the board locally aswell.
                } else if (sockLine.startsWith(" [") || sockLine.isEmpty() || sockLine.equals("\n")) { //If server sent some formatting that is meant for the terminal...
                    //Ignore these lines from the server
                } else {
                    if (sockLine.startsWith("Please enter a column number")) { //If the server says it is the players turn...
                        messageLabel.setText("Its your turn! Select a column to add piece"); //Update the information label to inform the user.
                    } else { //For anything else we simply set the information label to let the user know what the server sent...
                        messageLabel.setText(sockLine);
                        if (sockLine.startsWith("You are player")) { //If a new game has begun...
                            /*
                            The following nested for loop
                            clears and resets the game board for the start of a new game.
                             */
                            for (int row = 0; row < 6; row++) {
                                for (int col = 0; col < 7; col++) {
                                    JLabel cellToUpdate = GUIBoard[row][col];
                                    cellToUpdate.setText(""); //Clears any text
                                    cellToUpdate.setBackground(Color.WHITE); //Removes any background colour.
                                }
                            }
                        }
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("I think there's a problem with the host name.");
        } catch (IOException e) {
            System.out.println("Had an IO error for the connection.");
        }
    }

    /**
     * updateBoard() is responsible for updating the game board locally
     * and is called whenever the server sents an updated version of the game board.
     * It reads the servers output line by line until it reaches the end of the game board
     * and parses the terminal friendly version into the GUI version by updating the GUI cells.
     *
     * @param sockIn The BufferedReader of the server.
     * @throws IOException
     */
    private void updateBoard(BufferedReader sockIn) throws IOException {
        int row = 0;

        while (true) { //Loop repeats to read every line that the server is sending...
            String currentLine = sockIn.readLine();
            if (currentLine.equals("\n") || currentLine.isEmpty()) {
                row--;
            }
            if (currentLine.startsWith("-")) { //If we reached the end of the game board...
                break;
            } else if (currentLine.startsWith("|")) {
                int col = 0;
                /*
                This loop is used to determine the column in the line/row of each piece
                 received from the server.
                 */
                for (int i = 0; i < currentLine.length(); i++) {
                    if (currentLine.charAt(i) == 'X') { //If it is X make GUI piece red...
                        JLabel cellToUpdate = GUIBoard[row][col];
                        cellToUpdate.setBackground(Color.RED);
                        col++;
                    }
                    if (currentLine.charAt(i) == 'O') { //If it is O make GUI piece red...
                        JLabel cellToUpdate = GUIBoard[row][col];
                        cellToUpdate.setBackground(Color.yellow);
                        col++;
                    }
                    if (currentLine.charAt(i) == ' ' && currentLine.charAt(i - 1) == ' ' && currentLine.charAt(i + 1) == ' ') {
                        col++;
                    } //Triple empty spaces in the text count as blank/unclaimed positions.
                }
            }
            row++;
        }
    }

    /**
     createGUI() is the method that creates and initiates the GUI when the program is first ran.
     It uses swing for most components such as JFrames, JPanels, and JLabels. This method just makes
     all the components visible and adds the connect 4 cells to the JLabel 2D Array. It does not
     update the board at all throughout the game.
     */
    private void createGUI(PrintWriter sockOut) {
        JFrame frame = new JFrame("Connect 4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        frame.add(panel);

        messageLabel = new JLabel("Waiting for other player...");
        messageLabel.setHorizontalAlignment(0); //Centers the text
        messageLabel.setFont(new Font("", Font.BOLD, 30));
        panel.add(messageLabel, BorderLayout.NORTH); //Adds the JLabel to the top ("North") of the JPanel.

        //Connect 4 board
        JPanel boardPanel = new JPanel(new GridLayout(6, 7)); //Created a 6 x 7 grid layout JPanel that will contain all the cells.
        panel.add(boardPanel, BorderLayout.CENTER); //Adds the boardPanel to the middle ("Center") of the JPanel.

        /*
        For each position in the grid layout JPanel these nested for loops
        create a "cell" JLabel and adds it to GUI JLabel 2D Array at their
        position.
         */
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                JLabel cell = new JLabel("");
                cell.setHorizontalAlignment(0);
                cell.setFont(new Font("", Font.BOLD, 80));
                cell.setBorder(new javax.swing.border.LineBorder(Color.BLACK));
                cell.setOpaque(true);
                cell.setBackground(Color.WHITE);
                GUIBoard[row][col] = cell;
                boardPanel.add(cell);
            }
        }

        // Creating the 7 button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7));
        panel.add(buttonPanel, BorderLayout.SOUTH); //Adding the buttonPanel to the bottom ("South") of the JPanel.

        /*
        For Loop creates a new JButton for each column
        and adds it to the button JPanel
         */
        for (int col = 0; col < 7; col++) {
            JButton button = new JButton("" + col);
            button.setPreferredSize(new Dimension(0, 40));
            buttonPanel.add(button);
            int column = col;

            /*
            Creates an ActionListener event for each button such that
            when it is clicked it sends the column number to the server.
             */
            button.addActionListener(e -> {
                sockOut.println(column);
            });
        }

        frame.setSize(1000, 800);
        frame.setVisible(true);
    }


    public static void main(String[] args) {
        new GUIClient();
    }
}