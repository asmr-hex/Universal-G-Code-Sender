# JavaScript Eval + Global Variables in Macros and gcode

## approach:
* implement a public key-value store in the UGS GUIBackend [i know where to do this.]
* implement a preprocessor which finds javascript expressions and flags what lines they are in [know how to do this kind of] -- this way we don't have to do real-time/in-flight processing of everyline, just the flagged lines.
* create a plugin (like the DRO plugin) to visualize/change values [i generally know the approach]
 * key-value pairs can be persisted in a settings file or just ephemeral (checkbox?)
* find where each line is sent off to the controller. add an "in-flight" processor to check if this outgoing line has been flagged as a javascript expression, if so, evaluate it and send out the evaluated line.

## references
* [Feature Request](https://github.com/winder/Universal-G-Code-Sender/issues/1426)
