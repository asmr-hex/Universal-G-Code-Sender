# JavaScript Eval + Global Variables in Macros and gcode

## approach:
* implement a public key-value store in the UGS GUIBackend [i know where to do this.]
* implement a preprocessor which finds javascript expressions and flags what lines they are in [know how to do this kind of] -- this way we don't have to do real-time/in-flight processing of everyline, just the flagged lines.
* create a plugin (like the DRO plugin) to visualize/change values [i generally know the approach]
 * key-value pairs can be persisted in a settings file or just ephemeral (checkbox?)
* find where each line is sent off to the controller. add an "in-flight" processor to check if this outgoing line has been flagged as a javascript expression, if so, evaluate it and send out the evaluated line.

## TODOS
* [ ] finish implementing checks and filtering in ExpressionEngine
* [ ] Add support for persisting some expression variables within application settings so things are lost when program restarts
* [ ] implement a plugin for visualizing/editing expression variables
* [x] implement a custom event class for ExpressionEngineEvents
* [x] subscribe ExpressionEngine to events for changing machine/work locations. (make ExpressionEngine implement UGSEventListener)

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


## references
* [Feature Request](https://github.com/winder/Universal-G-Code-Sender/issues/1426)
