flappybird-ai
=============

This is a simple AI program that plays flappy bird for you. It currently gets around 10-20 points, although sometimes gets much better scores (40-100s).

<flappy bird ai gif>

##How does it work? 
It constantly takes screenshots of flappy bird running on an emulator on your computer, does extremely primitive object detection to build a model of the game, and then uses that model of the game to decide whether to "tap" the emulator screen or not (tells the OS to do a left-click, which translates to a tap on the emulator).

How do I run this?
=============

I apologize in advance; the setup is quite convoluted. If and when things go wrong, Google is your best bet. Feel free to contact me at hellocharlien@hotmail.com for any questions!

You need the following installed:

1. Scala (I use 2.10)
2. Processing 2.0.3
3. An emulator with flappybird installed
    1. I use the Android Development Kit (ADT)
    2. Download the flappybird apk [dropbox link here]( https://www.dropbox.com/s/1u0a8d1ug5yjvap/com.dotgears.flappybird.apk )
    3. The normal ADT emulator will run sluggishly. Please [Install HAXM](http://software.intel.com/en-us/android/articles/installation-instructions-for-intel-hardware-accelerated-execution-manager-windows) if you're on Windows. For Mac/Linux, your best bet is a different emulator software ([Genymotion]( http://www.genymotion.com/ ) looks promising).

### Setting up an emulator in the ADT

1. Unzip the ADT (directory should be named ```adt-bundle-<whatever>/```) and run the SDK Manager
2. Go to Android 4.0.3 (API15) and install the ```SDK Platform``` and ```Intel x86 Atom System Image```
2. After that installs, in the menu bar go to Tools -> Manage AVDs
3. Click New... and fill in the following values: <image here>
4. Press OK to save AVD definition
5. Press Start, and then press Launch. If all goes well the emulator should load: <image of emulator>
6. Put flappy bird on the emulator
    1. Download the flappybird apk [(dropbox link here)]( https://www.dropbox.com/s/1u0a8d1ug5yjvap/com.dotgears.flappybird.apk ) and place it into ```adt-bundle-<whatever>/sdk/platform-tools/```
    2. Start a terminal, navigate to ```adt-bundle-<whatever>/sdk/platform-tools/```
    3. Run ```adb install com.dotgears.flappybird.apk``` (emulator must be running)
    3. If these instructions didn't work check [http://stackoverflow.com/questions/3480201/how-do-you-install-an-apk-file-in-the-android-emulator](http://stackoverflow.com/questions/3480201/how-do-you-install-an-apk-file-in-the-android-emulator) for more info
1. Go back to your emulator; you should see flappy bird in the Apps view. Open it by clicking

Congrats, you've now got flappy bird on your emulator!

### Running the actual code *** Warning: extreme jankiness incoming ***

1. ```git clone https://github.com/hellochar/flappybird-ai && cd flappybird-ai```
3. Compile and run ```src/Main.scala```. Make sure you have the processing 2.0.3 core libraries in your classpath (located at ```processing\core\library\core.jar```). The processing window should pop up.
2. The code expects the emulator to be positioned in the top-left of your screen. Move the emulator to that position (or as close as possible).
5. You need to start flappy bird on your emulator and start the game manually. You'll also need to manually tap (click) at the beginning stretch when there are no pipes. The Processing window will display relevant info when it starts detecting pipes, at which point you can stop tapping manually.
6. **You must leave your mouse over the emulator window in order for mouse-clicks to register as taps in the game.**

## FAQ

#### * Why do you perform actions based on projected location?
> There's a bit of delay between when Robot.mousedown() is called and when flappy bird actually registers a click (empirically this was about 3 frames on my machine so that's what it is in the code).

#### * What's the basic AI strategy? 
> Look at the next pipe you have to fly over. If you're too close to the bottom lip, fly up. There's a bottom threshold to be a bit more safe since the relationship between taking action in your current frame and seeing that action reflected in the actual game is nondeterministic, since it's two different processes - you have to take into account things like GC in both Java and the emulator, load on your computer which may make either program stutter, and the base difference in framerate between the two programs.

#### * This is a disgusting abhorrence of good coding practice and you should go die in a fire
> Yeah pretty much

As always, contact me here on Github or at hellocharlien@hotmail.com for any questions, comments, or concerns!
