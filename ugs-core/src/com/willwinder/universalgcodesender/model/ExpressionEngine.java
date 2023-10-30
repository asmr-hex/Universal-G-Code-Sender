/*
    Copyright 2023 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.Settings;


import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.function.BiFunction;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for handling parsing of Javascript expressions.
 *
 * @author coco
 */
public class ExpressionEngine {
    public Pattern pattern = Pattern.compile("\\$\\{[^}]+\\}");

    private  Bindings variables = null;
    private IController controller = null;
    private Settings settings = null;
    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptEngine engine = null;

    public class BuiltinVariables {
        public static final String MachineX = "machine_x";
        public static final String MachineY = "machine_y";
        public static final String MachineZ = "machine_z";
        public static final String WorkX    = "work_x";
        public static final String WorkY    = "work_y";
        public static final String WorkZ    = "work_z";
        public static HashMap<String, BiFunction<ControllerStatus, Units, String>> getters;
        static {
            getters = new HashMap<>();
            getters.put(MachineX, (ControllerStatus status, Units units) -> Double.toString(status.getMachineCoord().getPositionIn(units).get(Axis.X)));
            getters.put(MachineY, (ControllerStatus status, Units units) -> Double.toString(status.getMachineCoord().getPositionIn(units).get(Axis.Y)));
            getters.put(MachineZ, (ControllerStatus status, Units units) -> Double.toString(status.getMachineCoord().getPositionIn(units).get(Axis.Z)));
            getters.put(WorkX, (ControllerStatus status, Units units) -> Double.toString(status.getWorkCoord().getPositionIn(units).get(Axis.X)));
            getters.put(WorkY, (ControllerStatus status, Units units) -> Double.toString(status.getWorkCoord().getPositionIn(units).get(Axis.Y)));
            getters.put(WorkZ, (ControllerStatus status, Units units) -> Double.toString(status.getWorkCoord().getPositionIn(units).get(Axis.Z)));
        }

        public static void update(ControllerStatus status, Units units, Bindings bindings) {
            for (Map.Entry<String, BiFunction<ControllerStatus, Units, String>> entry : getters.entrySet()) {
                bindings.put(entry.getKey(), entry.getValue().apply(status, units).toString());
            }
        }
    }


    public ExpressionEngine() {
        // TODO: maybe ingest UGS settings to see if we have persisted any variables

        this.engine = mgr.getEngineByName("JavaScript");
        this.variables = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    }

    public void connect(IController controller, Settings settings) {
        this.controller = controller;
        this.settings = settings;
    }

    public String eval(String expression) throws Exception {
        return this.engine.eval(expression).toString();
    }

    public void put(String key, String value) throws Exception {
        // TODO filter on builtins.

        // use engine.eval instead of variables.put to implicitly coerce types in javascript
        this.engine.eval(String.format("%s = %s", key, value));
    }

    public String get(String key) {
        return this.variables.get(key).toString();
    }

    /**
     * This takes a single command line string and evaluates contained expressions
     * if any are present. If any variables contained within the expressions cannot
     * be resolved, i.e. they have not been defined, then this method throws an
     * exception. This should halt the running program.
     *
     * @returns modified command line string with all expressions evaluated
     * @throws Exception if variables cannot be resolved, or if expression is invalid
     */
    public String process(String commandText) throws Exception {
        StringBuilder result = new StringBuilder();
        Matcher matcher = pattern.matcher(commandText);
        boolean expressionsFound = false;
        while (matcher.find()) {
            if (!expressionsFound) {
                // before the first expression is evaluated, we must sync all builtins to
                // the scripting scope.
                BuiltinVariables.update(this.controller.getControllerStatus(), this.settings.getPreferredUnits(), this.variables);
                expressionsFound = true;
            }

            String match = matcher.group();
            String expression = match.substring(2, match.length() - 1);

            // TODO ensure that expression isn't mutating internal variables

            // evaluate the expression in the JavaScript engine.
            String evaluated = eval(expression);

            // TODO if evaluated is null....raise

            // if the entire command is just an expression, put it in comments so it
            // isn't evaluated by the controller.
            if (match.trim().equals(commandText.trim())) {
                evaluated = String.format("(%s -> %s)", expression.trim(), evaluated);
            }

            matcher.appendReplacement(result, evaluated);
        }
        matcher.appendTail(result);

        return result.toString().trim();
    }
}
