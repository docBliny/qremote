QRemote
=======

QRemote is an Android application for the Nexus Q that provides a RESTful remote control API and 
web application.

Installation
------------
Currently the Nexus Q does not provide a supported way to install applications. Thus, you
must use the Android ADB tool to install and start QRemote:

    adb install QRemote.apk
    adb shell am start -a android.intent.action.MAIN -n com.blinnikka.android.qremote/.StartServiceActivity

Usage
-----
The application starts an HTTP server on port 8080. You may either use your web browser to navigate
to `http://[NexusQ-IP-Address]:8080` or use a REST client to access the API. Details on the API
are available on the built-in web page `http://[NexusQ-IP-Address]:8080/api.html`. 

Project Structure
-----------------
There are two separate projects that together create the QRemote application:
1. QRemote (Android application source)
1. QRemote Web (HTML-based web interface)

The web application is intended to be built using the HTML5 Boilerplate ANT build script
(https://github.com/h5bp/ant-build-script). The actual build files are not in the QRemote repo,
but it does contain the project specific configuration files and a couple of Windows BAT utility
files.

Building
--------
Assuming you have a working Android development environment, such as Eclipse set up, you should be
simply able to check out the code and import it into your IDE.

To make changes to the web interface, you will need to download the h5bp-ant-script into the
`QRemote Web/build` folder. Once any changes have been made, run the ant build, and then copy
the resulting files from the `QRemote Web/publish` folder into the `QRemote/assets/htdocs` folder.

Contact
-------
Additional information available on http://bliny.net
