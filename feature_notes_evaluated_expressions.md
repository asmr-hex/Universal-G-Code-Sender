# JavaScript Eval + Global Variables in Macros and gcode

## approach:
* implement a public key-value store in the UGS GUIBackend [i know where to do this.]
* implement a preprocessor which finds javascript expressions and flags what lines they are in [know how to do this kind of] -- this way we don't have to do real-time/in-flight processing of everyline, just the flagged lines.
* create a plugin (like the DRO plugin) to visualize/change values [i generally know the approach]
 * key-value pairs can be persisted in a settings file or just ephemeral (checkbox?)
* find where each line is sent off to the controller. add an "in-flight" processor to check if this outgoing line has been flagged as a javascript expression, if so, evaluate it and send out the evaluated line.

## TODOS
* [x] finish implementing checks and filtering in ExpressionEngine
* [x] Add support for persisting some expression variables within application settings so things are lost when program restarts
* [x] implement a plugin for visualizing/editing expression variables
* [x] implement a custom event class for ExpressionEngineEvents
* [x] subscribe ExpressionEngine to events for changing machine/work locations. (make ExpressionEngine implement UGSEventListener)
* [ ] process expressions on their way out from processed gcode files

## impl details
dont call ExpressionEngine.process() in GUIBackend.sendGcodeCommand (L257), but call it from
within controller.createCommand(String commandString). this might allow us to only call process
in one place. actually no, we need the calll site to be as close to sending the commnd as possible to ensure
the previous command has been sent and is done.
### buffered communicator expression processing [YES]
* we want to guarantee that an expression is evaluated once the previous command has completed on the controller.
  * can we subscribe to command finished? are commands sent when the previous have completed?
#### how to inject Expression Engine into Controller.Communicator
~~* in GUIBackend.fetchControllerFromFirmware (L194) we get a controller~~
~~* this gets it from FirmwareUtils.getControllerFor();~~
call controller.getCommunicator
add setExpressionengine to ICommunicator/AbstractCommunicator
or add a method for adding string processors to Abstract Communicator
### OR add in-flight expression evaluation right at the Connection level. [NO]
Connection.sendStringToComm is the last place before the actually commands are sent out
to the device. the only issue is that it is an interface method. there is an AbstractConnection
class which doesn't implement the `sendStringToComm` method, but all the actual implementations
*do* implement it....so we would have to maybe do a base implementation and then call super in
actual (non-abstract) implementations.
### actually just do itr in buffered communicator [YES]
why? because all communicators implemented extend buffered communicator and we only have to
insert a call in one plce. also, all calls to sendCommandImmediately also eventually call
buffered communicator.streamCommands


## commands
run ExpressionEngine tests
``` shell
# compile ugs-core with tests
cd ugs-core/ && mvn clean test-compile && cd ..
# run only ExpressionEngine tests
mvn surefire:test -Dtest=ExpressionEngineTest -pl :ugs-core
```

compile expressionengine plugin (with tests)
``` shell
cd ugs-platform/ugs-platform-plugin-expression-engine/ && mvn clean test-compile && cd ../..
```

install expression engine module
``` shell
cd ugs-platform/ugs-platform-plugin-expression-engine/ && mvn clean install && cd ../..
```
after doing this, you need to go to the plugins download tab and update it...there might be a faster way.

run ugs platform
``` shell
mvn nbm:run-platform -pl ugs-platform/application
```

NOTE: the jogging bug seems unrelated to the expression stuff. it basically happens when i plug in a joystick and activate the joystick. it seems like something
gets overwhelmed with the amount of messages being sent. also the joystick analog left seems stuck and is likely spamming  UGS leading to something getting
overwhelmed. additionally, i A/B tested it and it happens on a the master branch also (with no expression engine stuff).

## references
* [Feature Request](https://github.com/winder/Universal-G-Code-Sender/issues/1426)
