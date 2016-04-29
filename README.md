# Let's Hangout
##### Adam Chaulk(acchaulk) and Zhongyuan Fu (zyfu0408)
This app is our final project for CS 528 at Worcester Polytechnic Institute. Let's Hangout is an
android application that allows user to organize events with others in their area

## Running Parse Server
First, you must obtain npm from: https://www.npmjs.com. Once it is installed, open a terminal and run:
- npm install -g parse-server
- npm install -g parse-dashboard

These commands will install the Parse Server and the Parse dashboard.

To start the server, you must run two commands:
- mongodb-runner start
- parse-server --appId hangout_id --masterKey hangout_key

In order to start the parse dashboard, you can run
- parse-dashboard --appId hangout_id --masterKey hangout_key --serverURL "http://localhost:1337/parse" --appName Hangout

You should be able to see the dashboard at: http://localhost:4040/. Here, you can track all of the objects the app creates,
such as user accounts, events, and event memberships.

## Running the app
In order to run the Android app with the server, you must the IP address in StarterApplication.java in the
"com.parse.hangout" package.

You can obtain your ip address by running "ifconfig" on Mac/Linux, or "ipconfig" on windows
Once the address is changed and the server is started, you can start the application.

