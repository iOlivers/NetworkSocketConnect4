package Server;

import java.net.*;
import java.io.*;

/**
 * This is the game thread class. All the logic and functionality of the connect 4 game is found here.
 * The thread is created with two clients passed into it and utilizes PrintWriters and BufferedReaders to
 * communicate with the clients. The general flow of the game is found in the while loops of run().
 * The program splits win check logic and player movement into their own methods.
 *
 */
public class GameThread extends Thread {
    private Socket client1, client2;

    private char[][] gameBoard = new char[6][7]; // 6 x 7 game board is structured as a 2d char array that will consist of either "X","O", or " "

    private int placedRow = 0; //The row index of the last piece that was placed.
    private int placedCol = 0; //The column index of the last piece that was placed.

    public GameThread(Socket c1, Socket c2) {
        client1 = c1;
        client2 = c2;
    }

    public void run() {
        try (PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true); PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true); BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream())); BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));) {
            while (true) { //This for loop is the "main" game loop. It contains the entire flow of a game. It re-runs whenever a game has ended between the clients.
                /*
                For loop initializes/resets the game board at the start of a game by setting all
                positions in the 6 x 7 2D gameBoard array to ' ' which represents an empty/unclaimed position
                on the board.
                 */
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 7; col++) {
                        gameBoard[row][col] = ' '; //Set position to empty.
                    }
                }

                out1.println("You are player 1: 'X'");
                out2.println("You are player 2: 'O'");

                /*
                This while loop is the "turn" loop. Every turn it will print out the up-to-date
                game board to both clients, check if the last place piece created a win condition, if
                not it continues to let the other player create a move.
                 */
                while (true) {
                    printBoard(out1);
                    printBoard(out2);
                    if (winCheck('O') == 'O') { //Checks to see if 'O' Player won the game....
                        out2.println("You Win! New game starting in 5 seconds...");
                        out1.println("You Lose! New game starting in 5 seconds...");
                        break; //Breaking out of loop if player won...
                    }
                    out2.println("Waiting for other player...");
                    playerMove(in1, out1, 'X'); //Allow player 'X' to place a piece.
                    printBoard(out1);
                    printBoard(out2);
                    if (winCheck('X') == 'X') { //Checks to see if 'X' Player won the game...
                        out1.println("You Win! New game starting in 5 seconds...");
                        out2.println("You Lose! New game starting in 5 seconds...");
                        break; //Breaking out of loop if player won...
                    }
                    out1.println("Waiting for other player...");
                    playerMove(in2, out2, 'O'); //Allow player 'O' to place a piece.
                }
                Thread.sleep(5000); //After a game is ended, thread sleeps for 5 seconds before starting a new game... (So players know what happened)
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Ah nertz.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * printBoard() method is responsible for printing a nicely formatted terminal-friendly
     * version of the current gameBoard based on the 6 x 7 2D char array to
     * the player.
     *
     * @param out The Printer Writer of the player to print too.
     */
    private void printBoard(PrintWriter out) {
        out.println("-----------------------------"); //Start of game board output
        /*
        Nested for loop prints the gameBoard output in a 6 x 7 grid.
         */
        for (int row = 0; row < 6; row++) {
            out.print("| ");
            for (int col = 0; col < 7; col++) {
                out.print(gameBoard[row][col]); //Print game board piece at current position.
                out.print(" | ");
            }
            out.println();
            if (row < 6 - 1) {
                out.println();
            }
        }
        out.println("-----------------------------"); //End of game board output
        out.println(" [0] [1] [2] [3] [4] [5] [6]");
    }

    /**
     * playerMove() is used to allow the player to place a piece on the board.
     * It first clears any buffered input that the client may have input when it wasn't their turn.
     * It then prompts the client to input a column number to place in and verifies/sanitizes their input.
     * Lastly it finds the lowest empty spot in the column and adds the player's piece to it.
     *
     * @param in     BufferedReader of the client
     * @param out    PrinterWriter of the client
     * @param player The char symbol of the client (either 'X' or 'O')
     * @throws IOException
     */
    private void playerMove(BufferedReader in, PrintWriter out, Character player) throws IOException {

        while (in.ready()) { //Clears the clients buffered reader buffer in case they tried to place a piece when it wasn't their turn. (So it doesn't auto-place based on their previous input)
            in.readLine();
        }

        String playerInput; //The column to place given from the client

        int columnNum = -1; //The column to place the piece in.

        /*
        While loop is used for user input sanitization. It allows user to re-try endlessly if they
        input an invalid column number or some nonsense.
         */
        while (columnNum < 0 || columnNum > 6) {
            out.println("Please enter a column number (0-6) to place '" + player + "': ");
            playerInput = in.readLine();

            /*
            Try-catch is used in case a client sent some nonsense other than a number in
            their input.
             */
            try {
                columnNum = Integer.parseInt(playerInput);

                if (columnNum < 0 || columnNum > 6) { //If they input a column number that doesn't exist...
                    out.println("Invalid column number. Try again!");
                    columnNum = -1; //Reset columnNum so the while loop runs again.
                }
            } catch (Exception e) {
                out.println("Invalid Input. Try again!");
            }
        }

        /*
        This for loop is used for placing the players piece in the lowest empty spot
        in the column that they selected.
         */
        for (int row = 5; row >= 0; row--) {
            if (gameBoard[row][columnNum] == ' ') { //Checks to see if spot is empty...
                gameBoard[row][columnNum] = player; //Sets it to the player symbol
                placedRow = row; //Keeps track of last placed row.
                placedCol = columnNum; //Keeps track of last placed col.
                return; // Piece placed successfully, exit the method
            }
        }
        out.println("That column is full! Please select another one");
        playerMove(in, out, player); //Recursively allow players another turn in case their column was full.

    }

    /**
     * winCheck() method is used check whether the player has won. A win occurs when there are
     * 4 of the players pieces placed consecutively in a horizontal, vertical, or diagonal line.
     * It does this check based on the position of the last placed piece on the board.
     *
     * @param player The symbol of the player to check win for.
     * @return Win status - Returns player symbol if they have won such as 'X' or 'O'. Else if no win just ' '
     */
    private char winCheck(char player) {
        int pieceCount = 0; //Amount of players pieces found consecutively in line.

        /*
        HORIZONTAL LINE WIN CHECK
        The following for loop is used to check for horizontal line wins.
         */
        for (int col = 0; col < 7; col++) { //For every column...
            if (gameBoard[placedRow][col] == player) { //If the piece at the current column and last placed row belongs to the player...
                pieceCount++;
                if (pieceCount == 4) { //If we have 4 consecutive player pieces return with a win
                    return player;
                }
            } else {
                pieceCount = 0; //Reset back to 0 if it is not player (insures they are consecutively the same player not just 4 in the same column spread out)
            }
        }

        /*
        VERTICAL LINE WIN CHECK
        The following for loop is used to check for VERTICAL line wins.
        */
        pieceCount = 0;

        for (int row = placedRow; row < 6; row++) { //For every row...
            if (gameBoard[row][placedCol] == player) { //If the piece at the current row and last placed column belongs to the player...
                pieceCount++;
                if (pieceCount == 4) { //If we have 4 consecutive player pieces from last placed piece return with a win...
                    return player;
                }
            } else {
                break;
            }
        }


        /*
        DIAGONAL LINE WIN CHECK FROM TOP LEFT TO BOTTOM RIGHT

        The following for loop is used to check for DIAGONAL line wins going in the left (top) to right (bottom) direction.
         */

        pieceCount = 1;  // Including the last placed piece itself for diagonal checks.

        /*
        The first for loop checks up-left of the last placed piece.
         */
        for (int i = 1; i < 4; i++) {
            if (placedRow - i < 0 || placedCol - i < 0) { //If out of bounds...
                break;
            }
            if (gameBoard[placedRow - i][placedCol - i] != player) { //If a piece is not the players, break out of counting.
                break;
            }
            pieceCount++;
        }

        /*
        Now without resetting the pieceCount from our last loop, we got to check down-right of the last placed piece
         */
        for (int i = 1; i < 4; i++) {
            if (placedRow + i >= 6 || placedCol + i >= 7) { //If out of bounds...
                break;
            }
            if (gameBoard[placedRow + i][placedCol + i] != player) { //If a piece is not the players, break out of counting.
                break;
            }
            pieceCount++;
        }

        if (pieceCount >= 4) { //If our combined piece count from checking up-left and down-right of the current piece is 4+...
            return player;  //Return win!
        }

        pieceCount = 1;  //Reset for other diagonal direction, starting at 1 to include the last placed piece itself

        /*
        DIAGONAL LINE WIN CHECK FROM BOTTOM LEFT TO TOP RIGHT

        The following for loop is used to check for DIAGONAL line wins going in the right (top) to left (bottom) direction.
         */
        for (int i = 1; i < 4; i++) {
            if (placedRow + i >= 6 || placedCol - i < 0) { //If out of bounds...
                break;
            }
            if (gameBoard[placedRow + i][placedCol - i] != player) { //If a piece is not the players, break out of counting
                break;
            }
            pieceCount++;
        }

        /*
        Now without resetting the pieceCount from our last loop, we got to check up-right of the last placed piece
         */
        for (int i = 1; i < 4; i++) {
            if (placedRow - i < 0 || placedCol + i >= 7) { //If out of bounds...
                break;
            }
            if (gameBoard[placedRow - i][placedCol + i] != player) { //If a piece is not the players, break out of counting
                break;
            }
            pieceCount++;
        }

        if (pieceCount >= 4) { //If our combined piece count from checking up-left and down-right of the current piece is 4+...
            return player;  //Return player for the win.
        }


        return ' '; //No win
    }

}
