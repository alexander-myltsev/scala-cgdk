import model.{Hockeyist, World, Game, Move, ActionType}
import java.lang.StrictMath.PI

class MyStrategy extends Strategy {
  def move(self: Hockeyist, world: World, game: Game, move: Move): Unit = {
    move.setSpeedUp(-1.0D)
    move.setTurn(PI)
    move.setAction(ActionType.STRIKE)
  }
}
