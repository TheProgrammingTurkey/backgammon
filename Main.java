import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;
import java.util.List;

//pos 24 is the white Final
//pos -1 is the black Final
//pos 25 is the middle Capt row

public class Main extends PApplet {
    Board board = new Board();
    public String boardString = "2W00005B03B0005W5B0003W05W00002B0";
    boolean diceRolled = false;
    boolean selected = false;
    int selectedPiece = 100;
    int dice1 = 1;
    int dice2 = 1;
    boolean dice1used = false;
    boolean dice2used = false;
    boolean playing = false;
    boolean whiteFinal = false;
    boolean blackFinal = false;
    boolean gameOver = false;
    int blackDone = 0;
    int whiteDone = 0;
    int hoveredOver = 100;
    char curPlayer = 'W';
    //char curPlayer = 'B';
    int playerCoefficient = 1;
    int turnsLeft = 0;
    ArrayList<Piece> blackCaptured = new ArrayList<>();
    ArrayList<Piece> whiteCaptured = new ArrayList<>();
    int whiteCapt = 0;
    int blackCapt = 0;
    boolean curCapt = false;
    boolean curFinal = false;
    PImage[] diceImgs;

    public void settings() {
        size(840, 600);
    }

    public void setup() {
        //parsing the board string
        int whitePieces = 0, blackPieces = 0;
        for(int i = 0, pos = 0;  i < boardString.length(); i++, pos++){
            Tile tile = new Tile();
            if(pos < 24) board.tiles[pos] = tile;
            if(Integer.parseInt(boardString.substring(i, i+1)) > 0){
                int amt = Integer.parseInt(boardString.substring(i, i+1));
                for(int j = 0; j < amt; j++) {
                    Piece piece = new Piece(boardString.charAt(i + 1), pos);
                    if (boardString.charAt(i+1) == 'W' || boardString.charAt(i+1) == 'B'){
                        if(pos < 24) {
                            board.tiles[pos].occupants.add(piece);
                            if(boardString.charAt(i+1) == 'W') whitePieces++;
                            if(boardString.charAt(i+1) == 'B') blackPieces++;
                        }
                        else {
                            if(boardString.charAt(i+1) == 'W') {
                                whiteCapt++;
                                whitePieces++;
                                whiteCaptured.add(piece);
                            }
                            else {
                                blackCapt++;
                                blackPieces++;
                                blackCaptured.add(piece);
                            }
                        }
                    }
                }
                i++;
            }
        }
        blackDone += 15-blackPieces;
        whiteDone += 15-whitePieces;
        diceImgs = new PImage[] {loadImage("dice1.png"), loadImage("dice2.png"), loadImage("dice3.png"), loadImage("dice4.png"), loadImage("dice5.png"), loadImage("dice6.png")};
        textSize(30);
    }

    public void draw() {
        background(200);
        //getting what tile the mouse is over
        if (mouseY < 230 && (mouseX > 420 || mouseX < 360)) {
            if(mouseX > 780) hoveredOver = -1;
            else if (mouseX < 360) hoveredOver = 11 - (int) Math.floor(mouseX / 60);
            else hoveredOver = 5 + (int) Math.floor((420 - mouseX) / 60);
        } else if (mouseY > 380 && (mouseX > 420 || mouseX < 360)) {
            if(mouseX > 780) hoveredOver = 24;
            else if (mouseX < 360) hoveredOver = 12 + (int) Math.floor(mouseX / 60);
            else hoveredOver = 18 - (int) Math.floor((420 - mouseX) / 60);
        } else if(mouseX >= 360 && mouseX <= 420) hoveredOver = 25;
        else hoveredOver = -100;
        //seeing if the players can now start to score points
        int whiteTally = 0;
        int blackTally = 0;
        for (int i = 0; i < 18; i++) {
            if (!board.tiles[i].occupants.isEmpty())
                if (board.tiles[i].occupants.get(0).color == 'W') whiteTally++;
            if (!board.tiles[23 - i].occupants.isEmpty())
                if (board.tiles[23 - i].occupants.get(0).color == 'B') blackTally++;
        }
        whiteTally+=whiteCapt;
        blackTally+=blackCapt;
        whiteFinal = whiteTally == 0;
        blackFinal = blackTally == 0;
        if(whiteDone == 15 || blackDone == 15) {
            gameOver = true;
            playing = false;
            textSize(70);
        }
        //drawing the spot to put done pieces
        tileDrawing();
        pieceDrawing();
        if(!gameOver) {
            line(360, 0, 360, 600);
            line(420, 0, 420, 600);
        }
        if(curPlayer == 'W') fill(255);
        else fill(0);
        if(!diceRolled && !gameOver) text("Roll Dice", 20, 260);
        if(!gameOver) text("Turns Left: " + turnsLeft, 20, 290);
        if(gameOver && whiteDone == 15) text("Game Over: White Wins!", 50, 320);
        else if(gameOver && blackDone == 15) text("Game Over: Black Wins!", 50, 320);
        //drawing the dice
        if (playing){
            image(diceImgs[dice1 - 1], 300, 270, 50, 50);
            image(diceImgs[dice2 - 1], 450, 270, 50, 50);
        }
        else{
            image(diceImgs[0], 785, 250, 50, 50);
            image(diceImgs[0], 785, 310, 50, 50);
        }
    }
    public static class Board{
        Tile[] tiles;
        Piece[] pieces;
        Board(){
            tiles = new Tile[24];
            pieces = new Piece[30];
        }
    }
    public static class Tile{
        List<Piece> occupants;
        Tile(){
            occupants = new ArrayList<>();
        }
    }
    public static class Piece{
        char color;
        int tile;
        boolean selected;
        Piece(char col, int t){
            color = col;
            tile = t;
            selected = false;
        }

        @Override
        public String toString() {
            return String.format("%c, %d   ", color, tile);
        }
    }
    public List<Integer> findValidMoves(int selectedPiece){
        List<Integer> validMoves = new ArrayList<>();
        if(selectedPiece!=25){
            if (curPlayer == 'W' && selectedPiece != 100 && whiteFinal && ((23 < selectedPiece + dice1 && validFinalMoveDice1(selectedPiece)) || ((23 < selectedPiece + dice2 && validFinalMoveDice2(selectedPiece)))))
                validMoves.add(24);
            else if (curPlayer == 'B' && selectedPiece != 100 && blackFinal && ((0 > selectedPiece - dice1 && validFinalMoveDice1(selectedPiece)) || (0 > selectedPiece - dice2 && validFinalMoveDice2(selectedPiece))))
                validMoves.add(-1);
            else {
                for (int i = 0; i < 24; i++) {
                    if (((selectedPiece + dice1 * playerCoefficient == i && !dice1used && validFinalMoveDice1(selectedPiece)) || (selectedPiece + dice2 * playerCoefficient == i && !dice2used && validFinalMoveDice2(selectedPiece)))) {
                        if (board.tiles[i].occupants.isEmpty()) {
                            validMoves.add(i);
                        } else if (board.tiles[i].occupants.get(0).color == curPlayer || board.tiles[i].occupants.size() == 1) {
                            validMoves.add(i);
                        }
                    }
                }
            }
        }
        else{
            if(curPlayer == 'W') {
                if (board.tiles[dice1-1].occupants.isEmpty()) {
                    if (!dice1used) validMoves.add(dice1 - 1);
                }
                else if (board.tiles[dice1-1].occupants.get(0).color == curPlayer && !dice1used) validMoves.add(dice1-1);
                else if (board.tiles[dice1-1].occupants.size() == 1  && !dice1used) validMoves.add(dice1-1);
                if (board.tiles[dice2-1].occupants.isEmpty()) {
                    if (!dice2used) validMoves.add(dice2 - 1);
                }
                else if (board.tiles[dice2-1].occupants.get(0).color == curPlayer  && !dice2used) validMoves.add(dice2-1);
                else if (board.tiles[dice2-1].occupants.size() == 1  && !dice2used) validMoves.add(dice2-1);
            }
            if(curPlayer == 'B') {
                if (board.tiles[24-dice1].occupants.isEmpty()){
                    if(!dice1used) validMoves.add(24-dice1);
                }
                else if (board.tiles[24-dice1].occupants.get(0).color == curPlayer && !dice1used) validMoves.add(24-dice1);
                else if (board.tiles[24-dice1].occupants.size() == 1 && !dice1used) validMoves.add(24-dice1);
                if (board.tiles[24-dice2].occupants.isEmpty()) {
                    if (!dice2used) validMoves.add(24 - dice2);
                }
                else if (board.tiles[24-dice2].occupants.get(0).color == curPlayer  && !dice2used) validMoves.add(24-dice2);
                else if (board.tiles[24-dice2].occupants.size() == 1 && !dice2used) validMoves.add(24-dice2);
            }
        }
        return validMoves;
    }
    public boolean validFinalMoveDice1(int hoveredOver){
        int backwardsMoves = 6-dice1;
        if (!curFinal) return true;
        if(dice1used) return false;
        else if(curPlayer == 'W'){
            for(int i = 0; i < 6-dice1; i++) {
                if (board.tiles[18 + i].occupants.isEmpty()) {
                    backwardsMoves -= 1;
                }
                else if(board.tiles[18 + i].occupants.get(0).color != curPlayer){
                    backwardsMoves -= 1;
                }
            }
            int forwardMove = 0;
            for(int i = 23-dice1; i < 24; i++){
                if (!board.tiles[i].occupants.isEmpty()) {
                    if(board.tiles[i].occupants.get(0).color == curPlayer) {
                        forwardMove = i;
                        i = 24;
                    }
                }
            }
            if(!board.tiles[24-dice1].occupants.isEmpty()) {
                if(board.tiles[24-dice1].occupants.get(0).color == curPlayer) {
                    return hoveredOver + dice1 == 24;
                }
            }
            if(backwardsMoves > 0){
                return hoveredOver < (24-dice1);
            }
            return hoveredOver == forwardMove;
        }
        else{
            for(int i = 0; i < 6-dice1; i++) {
                if (board.tiles[5-i].occupants.isEmpty()) {
                    backwardsMoves -= 1;
                }
            }
            int forwardMove = 0;
            for(int i = dice1-2; i > -1; i--){
                if (!board.tiles[i].occupants.isEmpty()) {
                    forwardMove = i;
                    i = -1;
                }
            }
            if(!board.tiles[dice1-1].occupants.isEmpty()) {
                if(board.tiles[dice1-1].occupants.get(0).color == curPlayer) {
                    return hoveredOver - dice1 == -1;
                }
            }
            if(backwardsMoves > 0){
                return hoveredOver > (dice1-1);
            }
            return hoveredOver == forwardMove;
        }
    }
    public boolean validFinalMoveDice2(int hoveredOver){
        int backwardsMoves = 6-dice2;
        if (!curFinal) return true;
        if(dice2used) return false;
        else if(curPlayer == 'W'){
            for(int i = 0; i < 6-dice2; i++) {
                if (board.tiles[18 + i].occupants.isEmpty()) {
                    backwardsMoves -= 1;
                }
                else if (board.tiles[18 + i].occupants.get(0).color != curPlayer) {
                    backwardsMoves -= 1;
                }
            }
            int forwardMove = 0;
            for(int i = 23-dice2; i < 24; i++){
                if (!board.tiles[i].occupants.isEmpty()) {
                    if(board.tiles[i].occupants.get(0).color == curPlayer) {
                        forwardMove = i;
                        i = 24;
                    }
                }
            }
            if(!board.tiles[24-dice2].occupants.isEmpty()) {
                if(board.tiles[24-dice2].occupants.get(0).color == curPlayer) {
                    return hoveredOver + dice2 == 24;
                }
            }
            if(backwardsMoves > 0){
                return hoveredOver < (24 - dice2);
            }
            return hoveredOver == forwardMove;
        }
        else{
            for(int i = 0; i < 6-dice2; i++) {
                if (board.tiles[5-i].occupants.isEmpty()) {
                    backwardsMoves -= 1;
                }
            }
            int forwardMove = 0;
            for(int i = dice2-2; i > -1; i--){
                if (!board.tiles[i].occupants.isEmpty()) {
                    forwardMove = i;
                    i = -1;
                }
            }
            if(!board.tiles[dice2-1].occupants.isEmpty()) {
                if(board.tiles[dice2-1].occupants.get(0).color == curPlayer) {
                    return hoveredOver - dice2 == -1;
                }
            }
            if(backwardsMoves > 0){
                return hoveredOver > (dice2-1);
            }
            return hoveredOver == forwardMove;
        }
    }

    public boolean availableMove() {
        for(int i = 0; i < 24; i++){
            if(!board.tiles[i].occupants.isEmpty()){
                if(board.tiles[i].occupants.get(0).color == curPlayer && !findValidMoves(i).isEmpty() && !curCapt){
                    return true;
                }
            }
        }
        if(curCapt) return !findValidMoves(25).isEmpty();
        else return false;
    }
    public void mousePressed() {
        curCapt = (blackCapt > 0 && curPlayer == 'B') || (whiteCapt > 0 && curPlayer == 'W');
        if(gameOver) return;
        //role dice
        if(!diceRolled) {
            dice1used = false;
            dice2used = false;
            dice1 = (int) Math.floor(Math.random()*6)+1;
            dice2 = (int) Math.floor(Math.random()*6)+1;
            diceRolled = true;
            playing = true;
            if(dice1 == dice2) turnsLeft = 4;
            else turnsLeft = 2;
            if(!availableMove()) changeCurPlayer();
        }
        else if(!selected && ((hoveredOver > -1 && hoveredOver < 24) || (hoveredOver == 25 && curCapt))){
            if(!availableMove()) changeCurPlayer();
            if(hoveredOver == 25 && curCapt){
                selectedPiece = hoveredOver;
                selected = true;
                findValidMoves(selectedPiece);
            }
            else if (!board.tiles[hoveredOver].occupants.isEmpty() && !findValidMoves(hoveredOver).isEmpty() && (validFinalMoveDice1(hoveredOver) || validFinalMoveDice2(hoveredOver))) {
                if (board.tiles[hoveredOver].occupants.get(0).color == curPlayer && !curCapt) {
                    selectedPiece = hoveredOver;
                    selected = true;
                    findValidMoves(selectedPiece);
                }
            }
        }
        else if (selected){
            // if a piece is selected but not the capt spot
            if (findValidMoves(selectedPiece).contains(hoveredOver) && selectedPiece != 25) {
                Piece piece = board.tiles[selectedPiece].occupants.get(board.tiles[selectedPiece].occupants.size() - 1);
                board.tiles[selectedPiece].occupants.remove(board.tiles[selectedPiece].occupants.size() - 1);
                //captures
                if(hoveredOver != 24 && hoveredOver != -1) {
                    if (board.tiles[hoveredOver].occupants.size() == 1 && board.tiles[hoveredOver].occupants.get(0).color != piece.color) {
                        if (board.tiles[hoveredOver].occupants.get(0).color == 'W') {
                            whiteCapt++;
                            whiteCaptured.add(board.tiles[hoveredOver].occupants.get(0));
                        }
                        else {
                            blackCapt++;
                            blackCaptured.add(board.tiles[hoveredOver].occupants.get(0));
                        }
                        board.tiles[hoveredOver].occupants.remove(0);
                    }
                    board.tiles[hoveredOver].occupants.add(piece);
                }
                else if(hoveredOver == 24){
                    whiteDone++;
                }
                else {
                    blackDone++;
                }
                turnsLeft--;
                if (Math.abs(hoveredOver - selectedPiece) == dice1 && dice1 != dice2) dice1used = true;
                else if(Math.abs(hoveredOver - selectedPiece) == dice2 && dice1 != dice2) dice2used = true;
                else if(Math.abs(hoveredOver - selectedPiece) < dice1 && dice1 != dice2) dice1used = true;
                else if(Math.abs(hoveredOver - selectedPiece) < dice2 && dice1 != dice2) dice2used = true;
                if (turnsLeft > 0) {
                    selected = false;
                    selectedPiece = 100;
                    return;
                }
                changeCurPlayer();
            }
            else if(selectedPiece == 25 && curCapt && findValidMoves(25).contains(hoveredOver)){
                if(blackCapt > 0 && curPlayer == 'B'){
                    if(!board.tiles[hoveredOver].occupants.isEmpty()) {
                        if (board.tiles[hoveredOver].occupants.get(0).color == 'W' && board.tiles[hoveredOver].occupants.size() == 1) {
                            whiteCaptured.add(board.tiles[hoveredOver].occupants.get(0));
                            whiteCapt++;
                            board.tiles[hoveredOver].occupants.remove(0);
                        }
                    }
                    board.tiles[hoveredOver].occupants.add(blackCaptured.get(blackCaptured.size()-1));
                    blackCaptured.remove(blackCaptured.size()-1);
                    turnsLeft--;
                    blackCapt--;
                    if(24-hoveredOver == dice1) dice1used = true;
                    else dice2used = true;
                    if(turnsLeft > 0){
                        selected = false;
                        selectedPiece = 100;
                        return;
                    }
                    changeCurPlayer();
                }
                else if(whiteCapt > 0 && curPlayer == 'W'){
                    if(!board.tiles[hoveredOver].occupants.isEmpty()){
                        if(board.tiles[hoveredOver].occupants.get(0).color == 'B' && board.tiles[hoveredOver].occupants.size() == 1){
                            blackCaptured.add(board.tiles[hoveredOver].occupants.get(0));
                            blackCapt++;
                            board.tiles[hoveredOver].occupants.remove(0);
                        }
                    }
                    board.tiles[hoveredOver].occupants.add(whiteCaptured.get(whiteCaptured.size()-1));
                    whiteCaptured.remove(whiteCaptured.size()-1);
                    turnsLeft--;
                    whiteCapt--;
                    if(hoveredOver+1 == dice1) dice1used = true;
                    else dice2used = true;
                    if(turnsLeft > 0){
                        selected = false;
                        selectedPiece = 100;
                        return;
                    }
                    changeCurPlayer();
                }
            }
        }
    }
    public void pieceDrawing(){
        //draw pieces
        for(int i = 0; i < 24; i++){
            for(int j = 0; j < board.tiles[i].occupants.size(); j++){
                int pastMid = 0;
                //if the piece is hovered over by the mouse --> highlight yellow
                if(i == hoveredOver && j == board.tiles[i].occupants.size()-1 && diceRolled && board.tiles[i].occupants.get(0).color == curPlayer && !selected && !curCapt && (validFinalMoveDice1(hoveredOver) || validFinalMoveDice2(hoveredOver)) && !findValidMoves(hoveredOver).isEmpty()) {
                    fill(255, 255, 0);
                }
                else if(board.tiles[i].occupants.get(j).color == 'W') fill(255);
                else fill(50);
                //putting in captured place
                //each triangle is 60 wide
                //from x = 0 to x = 360
                //from x = 420 to x = 780

                //drawing pieces
                if (i < 12) {
                    if (i > 5) pastMid = 1;
                    ellipse(i * (-60) + 750 + pastMid * (-60), j * 15 + 20, 35, 35);
                } else {
                    if ((i - 12) > 5) pastMid = 1;
                    ellipse((i - 12) * 60 + 30 + pastMid * 60, j * (-15) + 580, 35, 35);
                }
            }
        }
        for(int i = 0; i < whiteCaptured.size(); i++){
            if(hoveredOver == 25 && curPlayer == 'W' && i == 0 && diceRolled) fill(255, 255, 0);
            else fill(255);
            ellipse(390, 200 + (i*20), 35, 35);
        }
        for(int i = 0; i < blackCaptured.size(); i++){
            if(hoveredOver == 25 && curPlayer == 'B' && i == 0 && diceRolled) fill(255, 255, 0);
            else fill(50);
            ellipse(390, 200 + ((i+whiteCapt)*20), 35, 35);
        }
        for(int i = 0; i < whiteDone; i++){
            fill(255);
            ellipse(810, 390 + 13*i, 35, 35);
        }
        for(int i = 0; i < blackDone; i++){
            fill(50);
            ellipse(810, 20 + 13*i, 35, 35);
        }

    }
    public void tileDrawing(){
        curCapt = (blackCapt > 0 && curPlayer == 'B') || (whiteCapt > 0 && curPlayer == 'W');
        curFinal = (blackFinal && curPlayer == 'B') || (whiteFinal && curPlayer == 'W');
        for(int i = 0; i < 24; i++) {
            int pastMid = 0;
            //draw top tiles
            if(i < 12){
                if (i > 5) pastMid = 1;
                //colors
                if(i%2 == 0) fill(255);
                else fill(210, 180, 140);
                //show possible moves
                if (findValidMoves(selectedPiece).contains(i)) fill(255, 255, 0);
                triangle(780 - 60*i - pastMid*60, 0, 750 - 60*i - pastMid*60, 230, 720 - 60*i - pastMid*60, 0);
            }
            //bottom row
            else {
                //colors
                if (i > 17) pastMid = 1;
                if(i%2 == 0) fill(255);
                else fill(210, 180, 140);
                //highlight possible moves
                if (findValidMoves(selectedPiece).contains(i)) fill(255, 255, 0);
                triangle(60*(i-12) + pastMid*60, 600, 30 + 60*(i-12) + pastMid*60, 380, 60 + 60*(i-12) + pastMid*60, 600);
            }
            //each triangle is 60 wide
            //from x = 0 to x = 360
            //from x = 420 to x = 780
            if(findValidMoves(selectedPiece).contains(24)) fill(255, 255,0);
            else fill(255);
            rect(780, 370, 840, 600);
            if(findValidMoves(selectedPiece).contains(-1)) fill(255, 255,0);
            else fill(210, 180, 140);
            rect(780, 0, 840, 240);
        }
    }
    public void changeCurPlayer(){
        diceRolled = false;
        //changing player
        if(curPlayer == 'W'){
            curPlayer = 'B';
            playerCoefficient = -1;
        }
        else{
            curPlayer = 'W';
            playerCoefficient = 1;
        }
        selected = false;
        selectedPiece = 100;
    }

    public static void main(String[] args) {
        PApplet.main("Main");
    }

}
