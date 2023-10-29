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
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.Settings;


import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A class for handling parsing of Javascript expressions.
 *
 * @author coco
 */
public class ExpressionEngine {
    private IController controller = null;
    private Settings settings = null;
    private ExpressionVariables variables = new ExpressionVariables();
    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptEngine engine = null;
    private Bindings bindings = null;
    private Pattern expressionPattern = Pattern.compile("(\\$\\{[^}]+\\})"); // TODO this isn't correct yet...

    private HashMap<String, Callable<String>> internalVariableUpdaters = HashMap<>() {
        {
            put("machine_x", (ControllerStatus status, Units units) -> status.getMachineCoord().getPositionIn(units).get(Axis.X));
        }}

    public ExpressionEngine() {
        // TODO: maybe ingest UGS settings to see if we have persisted any variables
        engine = mgr.getEngineByName("JavaScript");
        bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    }

    public void connect(IController controller, Settings settings) {
        this.controller = controller;
        this.settings = settings;
    }

    public ExpressionVariables getVariables() {
        return variables;
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
    public String eval(String commandText) throws Exception {
        StringBuilder result = new StringBuilder();
        Matcher matcher = expressionPattern.matcher(commandText);
        boolean expressionsFound = false;
        while (matcher.find()) {
            if (!expressionsFound) {
                // before the first expression is evaluated, update all variables
                // within the JavaScript scope to reflect any changes that have occured
                // in the UGS ExpressionVariables store. This is important, for example,
                // when we have JavaScript variables corresponding to internal states such
                // as machine X/Y/Z locations, etc.
                this.syncVariables();

                expressionsFound = true;
            }

            String expression = matcher.group(1);

            // TODO ensure that expression isn't mutating internal variables

            // TODO evaluate the expression in the JavaScript engine.

        }
        matcher.appendTail(result);

        if (expressionsFound) {
            // TODO somehow update the Expression Variables with updated bindings.
            // we could iterate over all new bindings? Bindings extends a Map (https://docs.oracle.com/javase/8/docs/api/javax/script/Bindings.html)
        }

        return result;
    }

    private void syncVariables() {
        // TODO update JavaScript & Expression internal variables from controller state
        ControllerStatus status = this.controller.getControllerStatus();
        Units units = this.settings.getPreferredUnits();
        // TODO for-loop iterating over internalVariableUpdaters


        // TODO update Expression Variables from JavaScript variables

    }
}
