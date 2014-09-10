[Quick-start simple strategy](http://russianaicup.ru/p/quick) ported to Scala
-----------------------------------------------------------------------------

```scala
import model.{Hockeyist, World, Game, Move, ActionType, HockeyistType, HockeyistState}

object MyStrategy {
  private def getNearestOpponent(x: Double, y: Double, world: World): Option[Hockeyist] = {
    val hockeists = world.getHockeyists.filterNot { hockeyist =>
      hockeyist.isTeammate || hockeyist.getType == HockeyistType.GOALIE ||
        hockeyist.getState == HockeyistState.KNOCKED_DOWN || hockeyist.getState == HockeyistState.RESTING
    }

    if (hockeists.isEmpty) None
    else Some(hockeists.minBy { hockeyist => math.hypot(x - hockeyist.getX, y - hockeyist.getY)})
  }

  private[MyStrategy] final val STRIKE_ANGLE = 1.0D * math.Pi / 180.0D
}

class MyStrategy extends Strategy {

  import MyStrategy.{STRIKE_ANGLE, getNearestOpponent}

  def move(self: Hockeyist, world: World, game: Game, move: Move): Unit = {
    if (self.getState == HockeyistState.SWINGING) {
      move.setAction(ActionType.STRIKE)
    } else {
      if (world.getPuck.getOwnerPlayerId == self.getPlayerId) {
        if (world.getPuck.getOwnerHockeyistId == self.getId) {
          val opponentPlayer = world.getOpponentPlayer
          val netX = 0.5D * (opponentPlayer.getNetBack + opponentPlayer.getNetFront)
          val netY = {
            val ny = 0.5D * (opponentPlayer.getNetBottom + opponentPlayer.getNetTop)
            (if (self.getY < ny) 0.5D else -0.5D) * game.getGoalNetHeight
          }
          val angleToNet = self.getAngleTo(netX, netY)
          move.setTurn(angleToNet)
          if (math.abs(angleToNet) < STRIKE_ANGLE) {
            move.setAction(ActionType.SWING)
          }
        } else {
          for (nearestOpponent <- getNearestOpponent(self.getX, self.getY, world)) {
            if (self.getDistanceTo(nearestOpponent) > game.getStickLength) {
              move.setSpeedUp(1.0D)
            } else if (math.abs(self.getAngleTo(nearestOpponent)) < 0.5D * game.getStickSector) {
              move.setAction(ActionType.STRIKE)
            } else {
              move.setTurn(self.getAngleTo(nearestOpponent))
            }
          }
        }
      } else {
        move.setSpeedUp(1.0D)
        move.setTurn(self.getAngleTo(world.getPuck))
        move.setAction(ActionType.TAKE_PUCK)
      }
    }
  }
}
```