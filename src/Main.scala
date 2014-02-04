import java.awt.event.InputEvent
import java.awt.Rectangle
import java.util.{TimerTask, Timer}
import processing.core._


/**
 * Created by IntelliJ IDEA.
 * User: hellochar
 * Date: 2/3/14
 * Time: 4:55 PM
 */

class Main extends PApplet {
  import PApplet._
  import PConstants._

  val robot = new java.awt.Robot()
  val screenRect = new Rectangle(2, 25, 480, 800)
  val birdMinX = 122
  val birdMaxX = 178

  val BOTTOM_THRESHOLD = 45
  val TOP_THRESHOLD = 80
  val TAP_COOLDOWN = 350
  
  val FLOOR = 640


  lazy val wallColor = color(84, 56, 71)
  lazy val birdOutlineColor = color(83, 56, 70)
  lazy val pipeShadowColor = color(85, 128, 34)
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

//    val (screenBuffered, time) = org.zhang.lib.time { robot.createScreenCapture(screenRect) }
    val screenBuffered = robot.createScreenCapture(screenRect)
//    println("Screen Capture took " + (time / 1e6f) + "ms!")
    val pimage = new PImage(screenBuffered)
    pimage.loadPixels()

    // get all the pixels of the pipe edges
    val pipeEdgesUnpruned = (birdMinX until 480).filter(x => pimage.pixels(FLOOR*width+x) == wallColor)

    //we've now got the right edges of each thick outline
    val pipeEdges = pipeEdgesUnpruned.filter(x => pipeEdgesUnpruned.contains(x + 1) && pipeEdgesUnpruned.contains(x - 1))

    // x must be pruned
    def findPipeOpening(x: Int) = {
      // find the pipe shadow
      val pipeShadow = (FLOOR until 0 by -1).find(y => pimage.pixels(y*width+x) == pipeShadowColor)


      pipeShadow.map{ shadow =>
        val pipeBottom = shadow - 37
        val pipeTop = pipeBottom - 159

//      val pipeTop = pipeShadow.get + 37 // the pipe top lip is 37 pixels high
//      val pipeBottom = pipeTop + 159 // the pipe is 159 pixels high

        (pipeTop, pipeBottom)
      }
    }

    image(pimage, 0, 0)

    stroke(0, 0, 255)
    pipeEdges.foreach(x => {
      findPipeOpening(x).foreach { case (pipeTop, pipeBottom) => {
        line(x, 0, x, pipeTop)
        line(x, FLOOR, x, pipeBottom)
      }}
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
      text(frameRate, 0, 0)
    }}
    projected(yourYOption).foreach { case (birdTop, birdBottom) => {
      noFill()
      stroke(128, 255, 0)
      rect(birdMinX, birdTop, birdMaxX, birdBottom)
    }}

    pipeEdges.headOption.foreach(firstPipe => {
      projected(yourYOption).foreach { case (birdTop, birdBottom) => {
        findPipeOpening(firstPipe).foreach { case (firstPipeTop, firstPipeBottom) => {

          if(firstPipeBottom - birdBottom < BOTTOM_THRESHOLD &&
             millis() - lastTapMillis > TAP_COOLDOWN) {
            // jump
            if(birdTop - firstPipeTop > TOP_THRESHOLD) {
              robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
              robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
              lastTapMillis = millis()
              fill(255, 0, 0)
              noStroke()
              ellipse(15, 700, 35, 35)
            }
          }

          if(millis() - lastTapMillis < TAP_COOLDOWN) {
            val diff = TAP_COOLDOWN - (millis() - lastTapMillis)
            noStroke(); fill(255, 255, 0)
            rectMode(CORNER)
            rect(0, 660, diff, 15)
          }

          stroke(255, 255, 0)
          line(0, firstPipeBottom, width, firstPipeBottom)
          stroke(255, 128, 0)
          line(0, firstPipeBottom - BOTTOM_THRESHOLD, width, firstPipeBottom - BOTTOM_THRESHOLD)

          stroke(0, 255, 129)
          line(0, firstPipeTop + TOP_THRESHOLD, width, firstPipeTop + TOP_THRESHOLD)
        } }

      } }

    })
//    if(pipeEdges.headOption.isDefined) {
//      saveFrame("frames/frames-####.tiff")
//    }
//    println(frameRate)
  }

  // scan down the center of your bounding box
  def findYourY(pimage: PImage) = {
    // the floor is at FLOOR pixels
    val birdOutlinePixels = (0 until FLOOR).filter(y =>
      pimage.pixels(y*width+(birdMinX + birdMaxX)/2) == birdOutlineColor
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