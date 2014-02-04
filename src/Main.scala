import java.awt.event.InputEvent
import java.awt.Rectangle
import java.util.{TimerTask, Timer}
import processing.core._
import org.zhang.lib._


/**
 * Created by IntelliJ IDEA.
 * User: hellochar
 * Date: 2/3/14
 * Time: 4:55 PM
 */

class Main extends MyPApplet {
  import PApplet._;
  import PConstants._;

  val robot = new java.awt.Robot()
  val screenRect = new Rectangle(103, 125, 480, 800)
  val birdMinX = 122
  val birdMaxX = 178

  val BOTTOM_THRESHOLD = 45
  val TOP_THRESHOLD = 80
  val TAP_COOLDOWN = 350


  lazy val wallColor = color(84, 56, 71)
  lazy val birdOutlineColor = color(83, 56, 70)
  lazy val tunnelShadowColor = color(85, 128, 34)
  lazy val eyeColor = color(250, 250, 250)

  var lastTapMillis: Int = 0
  var lastYMid = 0.0f
  var velocity = 0.0f
  var lastMillis = 0

  var lastOffset: Float = 0f

  override def setup() {
    size(480, 800, JAVA2D)
    frameRate(60)
  }

  override def draw() {
    val screenBuffered = robot.createScreenCapture(screenRect)
    val pimage = new PImage(screenBuffered)

    // get all the pixels of the tunnel edges
    val tunnelEdgesUnpruned = (birdMinX until 480).filter(x => pimage.get(x, 0) == wallColor)

    //we've now got the right edges of each thick outline
    val tunnelEdges = tunnelEdgesUnpruned.filter(x => tunnelEdgesUnpruned.contains(x + 1) && tunnelEdgesUnpruned.contains(x - 1))

    // x must be pruned
    def getTunnelInfo(x: Int) = {
      // find the tunnel shadow
      val tunnelShadow = (0 until 650).find(y => pimage.get(x, y) == tunnelShadowColor)

      val tunnelTop = tunnelShadow.get + 37 // the tunnel top lip is 37 pixels high
      val tunnelBottom = tunnelTop + 159 // the tunnel is 159 pixels high

      (tunnelTop, tunnelBottom)
    }

    image(pimage, 0, 0)

    stroke(0, 0, 255)
    tunnelEdges.foreach(x => {
      val (tunnelTop, tunnelBottom) = getTunnelInfo(x)
      line(x, 0, x, tunnelTop)
      line(x, 650, x, tunnelBottom)
    })

    val yourYOption = findYourY(pimage)
    yourYOption.foreach { case (birdTop, birdBottom) => {

      rectMode(CORNERS)
      noFill(); stroke(0, 255, 0)
      rect(birdMinX, birdTop, birdMaxX, birdBottom)



      val curYMid = (birdTop + birdBottom) / 2.0f
//      println(millis() + ", " + curYMid)
      val currentTime = millis()

      lastOffset = (curYMid - lastYMid)
      velocity = lastOffset / (currentTime - lastMillis)

      lastYMid = curYMid
      lastMillis = currentTime

      fill(255); stroke(255)
      textAlign(LEFT, TOP)
      text(velocity, 0, 0)
    }}
    projected(yourYOption).foreach { case (birdTop, birdBottom) => {
      noFill()
      stroke(128, 255, 0)
      rect(birdMinX, birdTop, birdMaxX, birdBottom)
    }}

    tunnelEdges.headOption.foreach(firstTunnel => {
      projected(yourYOption).foreach { case (birdTop, birdBottom) => {
        val (firstTunnelTop, firstTunnelBottom) = getTunnelInfo(firstTunnel)

        if(firstTunnelBottom - birdBottom < BOTTOM_THRESHOLD &&
           millis() - lastTapMillis > TAP_COOLDOWN) {
          // jump
          if(birdTop - firstTunnelTop > TOP_THRESHOLD) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            lastTapMillis = millis()
            fill(255, 0, 0)
            noStroke()
            ellipse(15, 700, 35, 35)
          }
        }

        stroke(255, 255, 0)
        line(0, firstTunnelBottom, width, firstTunnelBottom)
        stroke(255, 128, 0)
        line(0, firstTunnelBottom - BOTTOM_THRESHOLD, width, firstTunnelBottom - BOTTOM_THRESHOLD)

        stroke(0, 255, 129)
        line(0, firstTunnelTop + TOP_THRESHOLD, width, firstTunnelTop + TOP_THRESHOLD)
      } }

    })
    if(tunnelEdges.headOption.isDefined) {
      saveFrame("frames/frames-####.tiff")
    }
//    println(frameRate)
  }

  // scan down the center of your bounding box
  def findYourY(pimage: PImage) = {
    // the floor is at 650 pixels
    val birdOutlinePixels = (0 until 650).filter(y =>
      pimage.get((birdMinX + birdMaxX)/2, y) == birdOutlineColor
    )

//    val middleY = birdOutlinePixels.sum.toFloat / birdOutlinePixels.length
//    (middleY - 23, middleY + 23)
    if(birdOutlinePixels.isEmpty) {
      None
    } else {
      Some((birdOutlinePixels.min, birdOutlinePixels.max))
    }
  }

  def projected(pos: Option[(Int, Int)]) = {
    pos.map{ case (top, bottom) => (top + lastOffset, bottom + lastOffset) }
  }

}