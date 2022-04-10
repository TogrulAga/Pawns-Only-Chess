package chess

import kotlin.system.exitProcess

val PATTERN = Regex("([a-h][1-8]){2}")

enum class CoordinateX {
    a, b, c, d, e, f, g, h
}

enum class CoordinateY {
    `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`
}

data class Square(var marker: Char = ' ')


class Board(player1: Player, player2: Player) {
    private val squares: Array<Array<Square>> = Array(8) { Array(8) { Square() } }

    init {
        for (i in 0..7) {
            squares[i][1].marker = player1.pieceMarker
            squares[i][6].marker = player2.pieceMarker
        }
    }

    fun getSquare(x: Char, y: Char): Square {
        return squares[cX2i(x)][cY2i(y)]
    }

    fun getSquare(x: Int, y: Int): Square {
        return squares[x][y]
    }

    fun setSquare(x: Int, y: Int, square: Square) {
        squares[x][y] = square
    }

    fun printBoard() {
        for (y in 8 downTo 1) {
            println("  +---+---+---+---+---+---+---+---+")
            print("$y |")
            for (x in 0..7) {
                print(" ${squares[x][y - 1].marker} |")
            }
            println()
        }
        println("  +---+---+---+---+---+---+---+---+")
        println("    a   b   c   d   e   f   g   h\n")
    }

    fun countPieces(pieceMarker: Char): Int {
        var count = 0
        for (x in 0..7) {
            for (y in 0..7) {
                if (squares[x][y].marker == pieceMarker) {
                    count++
                }
            }
        }
        return count
    }
}

class Player(private val name: String, val pieceMarker: Char) {
    private var color: String = if (pieceMarker == 'W') "white" else "black"
    private var oppositeRank: Int = if (pieceMarker == 'W') 7 else 0
    private var opponent: Player? = null

    fun setOpponent(opponent: Player) {
        this.opponent = opponent
    }

    fun move(board: Board, lastSuccessfulMove: String): String {
        var move: String
        while (true) {
            println("$name's turn:")
            move = readln()

            if (move == "exit") {
                println("Bye!")
                exitProcess(0)
            }

            if (move.matches(PATTERN)) {
                if (board.getSquare(move[0], move[1]).marker == pieceMarker) {
                    if (board.getSquare(move[2], move[3]).marker == ' ') {
                        if ("${move[2]}${move[3]}" in possibleMoves("${move[0]}${move[1]}", board)) {
                            board.setSquare(cX2i(move[2]), cY2i(move[3]), board.getSquare(move[0], move[1]))
                            board.setSquare(cX2i(move[0]), cY2i(move[1]), Square())
                            break
                            } else if ("${move[2]}${move[3]}" in captureMoves("${move[0]}${move[1]}", board)) {
                                var enPx: Int
                                var enPy: Int

                                if (color == "white") {
                                    enPx = cX2i(move[2])
                                    enPy = cY2i(move[3]) - 1
                                } else {
                                    enPx = cX2i(move[2])
                                    enPy = cY2i(move[3]) + 1
                                }
                            if (cX2i(lastSuccessfulMove[0]) == enPx && cY2i(lastSuccessfulMove[1]) == enPy) {
                                board.setSquare(enPx, enPy, Square())
                                board.setSquare(cX2i(move[2]), cY2i(move[3]), board.getSquare(move[0], move[1]))
                                board.setSquare(cX2i(move[0]), cY2i(move[1]), Square())
                                break
                            } else {
                                println("Invalid Input")
                            }
                        } else {
                            println("Invalid Input")
                        }
                    } else if (board.getSquare(move[2], move[3]).marker == pieceMarker) {
                        println("Invalid Input")
                    } else {
                        if ("${move[2]}${move[3]}" in captureMoves("${move[0]}${move[1]}", board)) {
                            board.setSquare(cX2i(move[2]), cY2i(move[3]), board.getSquare(move[0], move[1]))
                            board.setSquare(cX2i(move[0]), cY2i(move[1]), Square())
                            break
                        } else {
                            println("Invalid Input")
                        }
                    }
                } else {
                    println("No $color pawn at ${move[0]}${move[1]}")
                }
            } else {
                println("Invalid Input")
            }
        }

        checkOppositeRankReached(board)

        checkAllPiecesCaptured(board)

        checkStalemate(board)

        return "${move[2]}${move[3]}"
    }

    private fun checkStalemate(board: Board) {
        var count = 0
        for (x in 0..7) {
            for (y in 0..7) {
                if (board.getSquare(x, y).marker == opponent?.pieceMarker) {
                    count += opponent!!.possibleMoves("${i2cX(x)}${i2cY(y)}", board).size
                    count += opponent!!.captureMoves("${i2cX(x)}${i2cY(y)}", board).size
                }
            }
        }

        if (count == 0) {
            board.printBoard()
            println("Stalemate!")
            exit()
        }
    }

    private fun checkAllPiecesCaptured(board: Board) {
        if (opponent?.let { board.countPieces(it.pieceMarker) } == 0) {
            board.printBoard()
            println("$color wins!")
            exit()
        }
    }

    /**
     * Checks if the opposite rank has been reached by any of the player's pawns.
     * If so, the game is over and player wins.
     */
    private fun checkOppositeRankReached(board: Board) {
        for (x in 0..7) {
            if (board.getSquare(x, oppositeRank).marker == pieceMarker) {
                board.printBoard()
                println("$color wins!")
                exit()
            }
        }
    }

    /**
     * Returns a list of possible moves for a given piece
     * @param moveStart the starting coordinate of the piece
     * @return a String list of possible moves
     */
    private fun possibleMoves(moveStart: String, board: Board): ArrayList<String> {
        val baseRow = if (color == "white") 1 else 6
        val x = cX2i(moveStart[0])
        val y = cY2i(moveStart[1])

        val moves = ArrayList<String>()

        if (color == "white") {
            // if there is a piece in front return empty list
            if (board.getSquare(x, y + 1).marker == opponent?.pieceMarker) return moves
            // If the piece is on the base row, it can move either one or two spaces
            if (y == baseRow) {
                moves.add("${i2cX(x)}${i2cY(y + 1)}")
                moves.add("${i2cX(x)}${i2cY(y + 2)}")
            } else {
                moves.add("${i2cX(x)}${i2cY(y + 1)}")
            }
        } else {
            // if there is a piece in front return empty list
            if (board.getSquare(x, y - 1).marker == opponent?.pieceMarker) return moves
            // If the piece is on the base row, it can move either one or two spaces
            if (y == baseRow) {
                moves.add("${i2cX(x)}${i2cY(y - 1)}")
                moves.add("${i2cX(x)}${i2cY(y - 2)}")
            } else {
                moves.add("${i2cX(x)}${i2cY(y - 1)}")
            }
        }
        return moves
    }

    /**
     * Returns all possible moves for a piece that can capture another piece
     * @param moveStart the starting coordinate of the piece
     * @return a String list of possible captures
     */
    private fun captureMoves(moveStart: String, board: Board): ArrayList<String> {
        val x = cX2i(moveStart[0])
        val y = cY2i(moveStart[1])

        val moves = ArrayList<String>()

        if (color == "white") {

            // Check if the piece can capture a piece 1 column to the right
            if (x in 0..6 && y in 0..6) {
                if (board.getSquare(x + 1, y + 1).marker == opponent?.pieceMarker) {
                    moves.add("${i2cX(x + 1)}${i2cY(y + 1)}")
                }
            }

            // Check if the piece can capture a piece 1 column to the left
            if (x in 1..7 && y in 0..6) {
                if (board.getSquare(x - 1, y + 1).marker == opponent?.pieceMarker) {
                    moves.add("${i2cX(x - 1)}${i2cY(y + 1)}")
                }
            }
        } else {
            // Check if the piece can capture a piece 1 column to the right
            if (x in 0..6 && y in 1..7) {
                if (board.getSquare(x + 1, y - 1).marker == opponent?.pieceMarker) {
                    moves.add("${i2cX(x + 1)}${i2cY(y - 1)}")
                }
            }

            // Check if the piece can capture a piece 1 column to the left
            if (x in 1..7 && y in 1..7) {
                if (board.getSquare(x - 1, y - 1).marker == opponent?.pieceMarker) {
                    moves.add("${i2cX(x - 1)}${i2cY(y - 1)}")
                }
            }
        }

        return moves
    }
}

object Chess {
    private val whitePlayer: Player
    private val blackPlayer: Player

    private val board: Board

    init {
        println("Pawns-Only Chess")
        println("First Player's name:")
        whitePlayer = Player(readln(), 'W')

        println("Second Player's name:")
        blackPlayer = Player(readln(), 'B')

        whitePlayer.setOpponent(blackPlayer)
        blackPlayer.setOpponent(whitePlayer)

        board = Board(whitePlayer, blackPlayer)
    }

    /**
     * Starts the game
     */
    fun play() {
        board.printBoard()
        var lastSuccessfulMove = "a0"
        while (true) {
            lastSuccessfulMove = whitePlayer.move(board, lastSuccessfulMove)
            board.printBoard()

            lastSuccessfulMove = blackPlayer.move(board, lastSuccessfulMove)
            board.printBoard()
        }
    }

}

/**
 * Converts an X coordinate in Char format to an Int that can be used to address a square in the board
 */
fun cX2i(x: Char): Int {
    return CoordinateX.valueOf(x.toString()).ordinal
}

/**
 * Converts an Y coordinate in Char format to an Int that can be used to address a square in the board
 */
fun cY2i(y: Char): Int {
    return CoordinateY.valueOf(y.toString()).ordinal
}

/**
 * Converts an X coordinate in Int format to a Char that can be used to print a square in the board
 */
fun i2cX(x: Int): String {
    return CoordinateX.values()[x].name
}

/**
 * Converts an Y coordinate in Int format to a Char that can be used to print a square in the board
 */
fun i2cY(y: Int): String {
    return CoordinateY.values()[y].name
}

fun exit() {
    println("Bye!")
    exitProcess(0)
}

fun main() {
    Chess.play()
}