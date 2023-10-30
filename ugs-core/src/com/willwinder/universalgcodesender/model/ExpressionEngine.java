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
import java.util.concurrent.Callable;
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
    private IController controller = null;
    private Settings settings = null;
    private ExpressionVariables variables = new ExpressionVariables();
    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptEngine engine = null;
    private Bindings bindings = null;
    private Pattern expressionPattern = Pattern.compile("(\\$\\{[^}]+\\})"); // TODO this isn't correct yet...


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
        int lastIndex = 0;
        while (matcher.find()) {
            if (!expressionsFound) {
                // before the first expression is evaluated, we must sync all builtins to
                // the scripting scope.
                this.syncBuiltinVariablesToScriptingScope();
                expressionsFound = true;
            }

            String expression = matcher.group(1);

            // TODO ensure that expression isn't mutating internal variables

            // evaluate the expression in the JavaScript engine.
            String evaluated = this.engine.eval(expression).toString();

            result.append(commandText, lastIndex, matcher.start()).append(evaluated);

            lastIndex = matcher.end();
        }
        matcher.appendTail(result);

        if (expressionsFound) {
            // update internal mapping with new state in scripting scope
            this.syncScriptingScopeToUserVariables();
        }

        return result.toString();
    }

    /**
     * Syncs builtin variables, such as machine positions, to the scripting scope.
     * Since builtin variables have a single source of truth (from the controller),
     * we don't have to worry about when we call this because the internal mapping
     * of builtin variables should always be the same as the bindings within the
     * JavaScript scope.
     */
    public void syncBuiltinVariablesToScriptingScope() throws Exception {
        // update builtin variables from controller status
        ControllerStatus status = this.controller.getControllerStatus();
        Units units = this.settings.getPreferredUnits();
        this.variables.builtin.update(status, units);

        // update builtin variables within JavaScript scope
        for (Map.Entry<String, String> entry : this.variables.builtin.variables.entrySet()) {
            this.engine.eval("%s = %s".format(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Syncs user variables from the internal map of defined variables to bindings
     * in the JavaScript scope. User defined variables can be set in two ways:
     *  1. within a scripted expression (e.g. "{myVar = 3.14}" in a Macro or in gcode).
     *  2. by directly putting a key/value pair within the ExpressionVariables user variables
     *     map (via the Expression Variables plugin for example).
     * Since user defined variables can have two sources, we must enforce rules so we
     * don't clobber values from either source.
     * Consequently, we should only allow updating the key/value pairs directly in the user
     * variables map when no gcode is being run and when the controller is Idle.
     */
    public void syncUserVariablesToScriptingScope() throws Exception {
        // TODO check if controller state is IDLE, if not return early to avoid any race conditions.

        // update builtin variables within JavaScript scope
        for (Map.Entry<String, String> entry : this.variables.entrySet()) {
            this.engine.eval("%s = %s".format(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Syncs bindings from the JavaScript scope to the internal expression variable mapping.
     * This can be done at anytime due to the restriction of only allowing syncing from
     * internal mapping to JS scope when the controller is in an IDLE state (i.e. no gcode lines
     * are being evaluated and sent).
     */
    public void syncScriptingScopeToUserVariables() {
        // iterate over all bindings and sync them to the variable mapping.
        for (Map.Entry<String, Object> entry : this.bindings.entrySet()) {
            // TODO filter out irrelevant bindings existing by default in JS scope.
            this.variables.set(entry.getKey(), entry.getValue().toString());
        }
    }
}
