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

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.EnabledPins;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.ExpressionEngineEvent;
import com.willwinder.universalgcodesender.uielements.components.PopupEditor;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.script.Bindings;

/**
 * DRO style display panel with current controller state.
 */
public class ExpressionEnginePanel extends JPanel implements UGSEventListener {
    private static final Logger LOGGER = Logger.getLogger(ExpressionEnginePanel.class.getName());
    private static final int COMMON_RADIUS = 7;


    private final RoundedPanel activeStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel activeStateValueLabel = new JLabel(" ");

    private VariablesTableModel variablesTableModel;
    private JTable variablesTable;

    private final BackendAPI backend;

    private Units units = null;

    public ExpressionEnginePanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
        }

        // initFonts();
        initComponents();
        initSizer();

        if (this.backend != null && this.backend.getSettings().getPreferredUnits() == Units.MM) {
            setUnits(Units.MM);
        } else {
            setUnits(Units.INCH);
        }

        // TODO updateVariables(variables)
    }

    private void initSizer() {
        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(160, 330),
                new Dimension(240, 420),
                new Dimension(310, 420));
        // sizer.addListener(fontManager::applyFonts);
    }

    // private void initFonts() {
    //     // fontManager.init();
    //     // GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    //     // fontManager.registerFonts(ge);
    // }

    private void initComponents() {
        String debug = "";
        setLayout(new MigLayout(debug + "fillx, wrap 1, inset 5", "grow"));

        activeStateValueLabel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStateValueLabel.setText("OFFLINE");

        activeStatePanel.setLayout(new MigLayout(debug + "fill, inset 0 5 0 5"));
        activeStatePanel.setBackground(Color.GREEN);
        activeStatePanel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStatePanel.add(activeStateValueLabel, "al center");
        activeStateValueLabel.setBorder(BorderFactory.createEmptyBorder());
        add(activeStatePanel, "growx");

        variablesTableModel = new VariablesTableModel(); // TODO pass ExpressionEngine?
        variablesTable = new JTable(variablesTableModel);
        JScrollPane sp = new JScrollPane(variablesTable);
        add(sp);

        // TODO set default values to table
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    private void setUnits(Units u) {
        if (u == null || units == u) return;
        units = u;
        switch (u) {
            case MM:
            case INCH:
                break;
            default:
                units = Units.MM;
                break;
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        // TODO listen for ExpressionEngine Update event
        if (evt instanceof ExpressionEngineEvent expressionEngineEvent) {
            updateVariables(expressionEngineEvent.getVariables());
        }
    }

    /**
     * Enable and disable the different axes based on capabilities and configuration.
     */
    private void updateVariables(Bindings variables) {
        if (!backend.isConnected()) {
            return;
        }
        // TODO
    }

    private void onControllerStatusReceived(ControllerStatus status) {
        // this.updateStatePanel(status.getState());
        // resetStatePinComponents();

        // updatePinStates(status);

        // this.setUnits(backend.getSettings().getPreferredUnits());

        // Arrays.stream(Axis.values())
        //         .filter(axisPanels::containsKey)
        //         .forEach(axis -> {
        //             if (status.getMachineCoord() != null) {
        //                 Position machineCoord = status.getMachineCoord().getPositionIn(units);
        //                 axisPanels.get(axis).setMachinePosition(machineCoord.get(axis));
        //             }

        //             if (status.getWorkCoord() != null) {
        //                 Position workCoord = status.getWorkCoord().getPositionIn(units);
        //                 axisPanels.get(axis).setWorkPosition(workCoord.get(axis));
        //             }
        //         });

        // // Use real-time values if available, otherwise show the target values.
        // int feedSpeed = status.getFeedSpeed() != null
        //         ? (int) (status.getFeedSpeed() * UnitUtils.scaleUnits(status.getFeedSpeedUnits(), backend.getSettings().getPreferredUnits()))
        //         : (int) this.backend.getGcodeState().feedRate;
        // this.feedValue.setText(Integer.toString(feedSpeed));

        // int spindleSpeed = status.getSpindleSpeed() != null
        //         ? status.getSpindleSpeed().intValue()
        //         : (int) this.backend.getGcodeState().spindleSpeed;
        // this.spindleSpeedValue.setText(Integer.toString(spindleSpeed));

        // GcodeState state = backend.getGcodeState();
        // if (state == null) {
        //     gStatesLabel.setText("--");
        // } else {
        //     gStatesLabel.setText(
        //             String.join(" ",
        //                     state.currentMotionMode.toString(),
        //                     state.units.toString(),
        //                     state.feedMode.toString(),
        //                     state.distanceMode.toString(),
        //                     state.offset.toString(),
        //                     state.plane.code.toString()));
        // }
    }
}
