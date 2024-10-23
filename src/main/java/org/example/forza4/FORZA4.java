package org.example.forza4;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class FORZA4 extends Application {
    private static final  int ROWS = 6;
    private static final  int COLS = 7;
    private static final int HUMAN_PLAYER = 1;  // Rosso
    private static final int AI_PLAYER = 2;     // Giallo
    private static final int MAX_DEPTH = 5;

    private int[][] board = new int[ROWS][COLS];  // Griglia di gioco
    private boolean isRedTurn = true;             // Indica se è il turno del giocatore umano (rosso)
    private Circle[][] cells;                     // Celle della griglia per l'interfaccia utente

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        cells = new Circle[ROWS][COLS];

        // Configurazione della griglia con celle circolari
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Circle cell = new Circle(40);
                cell.setFill(Color.WHITE);
                cell.setStroke(Color.BLACK);
                cells[row][col] = cell;

                int finalCol = col;  // Variabile per memorizzare la colonna cliccata
                cell.setOnMouseClicked(event -> handlePlayerMove(finalCol));  // Cattura il click del giocatore
                gridPane.add(cell, col, row);
            }
        }

        primaryStage.setScene(new Scene(gridPane));
        primaryStage.setTitle("Forza 4 con IA");
        primaryStage.show();
    }

    private void handlePlayerMove(int col) {
        if (isRedTurn) {
            int row = dropDisc(col, HUMAN_PLAYER);  // Mossa del giocatore
            if (row != -1) {
                cells[row][col].setFill(Color.RED);
                if (checkForWin(HUMAN_PLAYER)) {
                    endGame("Hai vinto!");
                    return;
                }
                isRedTurn = false;  // Passa il turno all'IA
                handleAIMove();
            }
        }
    }

    private void handleAIMove() {
        int bestCol = getBestMove();  // Mossa migliore calcolata dall'IA
        int row = dropDisc(bestCol, AI_PLAYER);
        if (row != -1) {
            cells[row][bestCol].setFill(Color.YELLOW);
            if (checkForWin(AI_PLAYER)) {
                endGame("L'IA ha vinto!");
                return;
            }
            isRedTurn = true;  // Passa il turno al giocatore
        }
    }

    private int dropDisc(int col, int player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = player;  // Assegna il disco alla cella disponibile
                return row;
            }
        }
        return -1;  // Colonna piena
    }

    private void undoMove(int row, int col) {
        board[row][col] = 0;  // Ripristina la cella
    }

    private boolean checkForWin(int player) {
        // Controllo vittoria in tutte le direzioni
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] == player &&
                        (checkDirection(row, col, 1, 0, player) || // Orizzontale
                                checkDirection(row, col, 0, 1, player) || // Verticale
                                checkDirection(row, col, 1, 1, player) || // Diagonale \
                                checkDirection(row, col, 1, -1, player))) { // Diagonale /
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDirection(int row, int col, int dRow, int dCol, int player) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int r = row + i * dRow;
            int c = col + i * dCol;
            if (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
                count++;
            } else {
                break;
            }
        }
        return count == 4;
    }

    private boolean isBoardFull() {
        for (int col = 0; col < COLS; col++) {
            if (board[0][col] == 0) {
                return false;
            }
        }
        return true;
    }

    private int getBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestCol = -1;

        for (int col = 0; col < COLS; col++) {
            int row = dropDisc(col, AI_PLAYER);
            if (row != -1) {
                int score = minimax(0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                undoMove(row, col);
                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                }
            }
        }
        return bestCol;
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (checkForWin(AI_PLAYER)) return 1000 - depth;  // Vittoria IA
        if (checkForWin(HUMAN_PLAYER)) return -1000 + depth;  // Vittoria giocatore
        if (isBoardFull() || depth == MAX_DEPTH) return evaluateBoard();  // Griglia piena o profondità massima

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < COLS; col++) {
                int row = dropDisc(col, AI_PLAYER);
                if (row != -1) {
                    int eval = minimax(depth + 1, false, alpha, beta);
                    undoMove(row, col);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;  // Potatura alpha-beta
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < COLS; col++) {
                int row = dropDisc(col, HUMAN_PLAYER);
                if (row != -1) {
                    int eval = minimax(depth + 1, true, alpha, beta);
                    undoMove(row, col);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;  // Potatura alpha-beta
                }
            }
            return minEval;
        }
    }

    private int evaluateBoard() {
        // Funzione di valutazione della griglia (semplice per ora)
        int score = 0;
        score += evaluateDirection(1, 0, AI_PLAYER);  // Orizzontale IA
        score += evaluateDirection(0, 1, AI_PLAYER);  // Verticale IA
        score += evaluateDirection(1, 1, AI_PLAYER);  // Diagonale \ IA
        score += evaluateDirection(1, -1, AI_PLAYER); // Diagonale / IA

        score -= evaluateDirection(1, 0, HUMAN_PLAYER);  // Penalizzazione giocatore
        score -= evaluateDirection(0, 1, HUMAN_PLAYER);
        score -= evaluateDirection(1, 1, HUMAN_PLAYER);
        score -= evaluateDirection(1, -1, HUMAN_PLAYER);

        return score;
    }

    private int evaluateDirection(int dRow, int dCol, int player) {
        int score = 0;
        // Aggiungi logica per valutare la griglia in base a quanti dischi consecutivi ha il giocatore
        return score;
    }

    private void endGame(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
        System.exit(0);  // Uscita dal gioco
    }

    public static void main(String[] args) {
        launch(args);
    }
}
