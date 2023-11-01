/*
    Copyright 2016-2023 Will Winder

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
package com.willwinder.ugs.nbp.expressionengine.panels;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.model.events.ExpressionEngineEvent;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.logging.Logger;
import javax.script.Bindings;

/**
 * DRO style display panel with current controller state.
 */
public class ExpressionEnginePanel extends JPanel implements UGSEventListener {
    private static final Logger LOGGER = Logger.getLogger(ExpressionEnginePanel.class.getName());

    private VariablesTableModel model;
    private JTable variablesTable;

    private final BackendAPI backend;

    public ExpressionEnginePanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);

            model = new VariablesTableModel(this.backend.getExpressionEngine());
        }

        initComponents();
        initSizer();
    }

    private void initSizer() {
        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(160, 330),
                new Dimension(240, 420),
                new Dimension(310, 420));
    }

    private void initComponents() {
        String debug = "";
        setLayout(new MigLayout(debug + "fillx, wrap 1, inset 5", "grow"));

        variablesTable = new JTable(model);
        JScrollPane sp = new JScrollPane(variablesTable);
        add(sp);

        model.update();
    }

    @Override
    public void setEnabled(boolean enabled) {}

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ExpressionEngineEvent) {
            model.update();
        }
    }
}
