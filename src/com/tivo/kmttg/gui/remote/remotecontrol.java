/*
 * Copyright 2008-Present Kevin Moye <moyekj@yahoo.com>.
 *
 * This file is part of kmttg package.
 *
 * kmttg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tivo.kmttg.gui.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Stack;

import com.tivo.kmttg.JSON.JSONArray;
import com.tivo.kmttg.JSON.JSONException;
import com.tivo.kmttg.JSON.JSONObject;
import com.tivo.kmttg.JSON.JSONTokener;
import com.tivo.kmttg.main.config;
import com.tivo.kmttg.main.telnet;
import com.tivo.kmttg.rpc.Remote;
import com.tivo.kmttg.util.file;
import com.tivo.kmttg.util.log;
import com.tivo.kmttg.util.string;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class remotecontrol {
   public VBox panel = null;
   public ChoiceBox<String> tivo = null;
   public ChoiceBox<String> hme = null;
   public ChoiceBox<String> hme_sps = null;
   public TextField jumpto_text = null;
   public TextField jumpahead_text = null;
   public TextField jumpback_text = null;
   public Boolean cc_state = false;
   
   // These buttons selectively disabled
   public Button hme_button = null;
   public Button sps_button = null;
   public Button jumpto_button = null;
   public Button jumpahead_button = null;
   public Button jumpback_button = null;
   String background = config.gui.getWebColor(Color.BLACK);
   String text_color = config.gui.getWebColor(Color.WHITE);
   
   public remotecontrol (final Stage frame) {
      // Remote Control Tab items
      panel = new VBox();
      panel.setStyle("-fx-background-color: " + background);
      
      Pane panel_controls = new Pane();
      panel_controls.setStyle("-fx-background-color: " + background);
      
      // TiVo Remote control panel
      final Object[][] Buttons = {
         {"back",        "back.png",         0.6,  20,   0,  0, 0, "AltZ",      KeyCode.K},
         {"channelUp",   "channel_up.png",   0.5,  20,  25,  0, 0, "PAGE_UP",   KeyCode.PAGE_UP},
         {"lab_channel", "channel_label.png",0.7,  20,  55,  0, 0, null,        null},
         {"channelDown", "channel_down.png", 0.5,  20,  70,  0, 0, "PAGE_DOWN", KeyCode.PAGE_DOWN},
         {"left",        "left.png",         0.5,  15,  85, 20, 0, "LEFT",      KeyCode.LEFT},
         {"zoom",        "zoom.png",         0.7,  20, 130,  0, 0, "AltZ",      KeyCode.Z},
         {"tivo",        "tivo.png",         0.7,  60,   0,  0, 0, "AltT",      KeyCode.T},
         {"up",          "up.png",           0.5,  65,  40, 20, 0, "UP",        KeyCode.UP},
         {"select",      "select.png",       0.5,  70,  85,  0, 0, "AltS",      KeyCode.S},
         {"down",        "down.png",         0.5,  65, 125, 20, 0, "DOWN",      KeyCode.DOWN},
         {"liveTv",      "livetv.png",       0.7, 115,  20,  0, 0, "AltL",      KeyCode.L},
         {"info",        "info.png",         0.7, 115,  55,  0, 0, "AltI",      KeyCode.I},
         {"right",       "right.png",        0.5, 120,  85, 20, 0, "RIGHT",     KeyCode.RIGHT},
         {"guide",       "guide.png",        0.7, 115, 130,  0, 0, "AltG",      KeyCode.G},
         {"num1",        "1.png",            0.7, 200,   0, 10, 0, "1",         KeyCode.DIGIT1},
         {"num2",        "2.png",            0.7, 245,   0, 10, 0, "2",         KeyCode.DIGIT2},
         {"num3",        "3.png",            0.7, 290,   0, 10, 0, "3",         KeyCode.DIGIT3},
         {"num4",        "4.png",            0.7, 200,  35, 10, 0, "4",         KeyCode.DIGIT4},
         {"num5",        "5.png",            0.7, 245,  35, 10, 0, "5",         KeyCode.DIGIT5},
         {"num6",        "6.png",            0.7, 290,  35, 10, 0, "6",         KeyCode.DIGIT6},
         {"num7",        "7.png",            0.7, 200,  70, 10, 0, "7",         KeyCode.DIGIT7},
         {"num8",        "8.png",            0.7, 245,  70, 10, 0, "8",         KeyCode.DIGIT8},
         {"num9",        "9.png",            0.7, 290,  70, 10, 0, "9",         KeyCode.DIGIT9},
         {"clear",       "clear.png",        0.7, 200, 105, 10, 0, "DELETE",    KeyCode.DELETE},
         {"num0",        "0.png",            0.7, 245, 105, 10, 0, "0",         KeyCode.DIGIT0},
         {"enter",       "enter.png",        0.7, 290, 105, 10, 0, "ENTER",     KeyCode.ENTER},
         {"actionA",     "A.png",            0.7, 185, 135, 10, 0, "AltA",      KeyCode.A},
         {"actionB",     "B.png",            0.7, 225, 135, 10, 0, "AltB",      KeyCode.B},
         {"actionC",     "C.png",            0.7, 265, 135, 10, 0, "AltC",      KeyCode.C},
         {"actionD",     "D.png",            0.7, 305, 135, 10, 0, "AltD",      KeyCode.D},
         {"thumbsDown",  "thumbsdown.png",   0.7, 355,   0, 10, 0, "SUBTRACT",  KeyCode.SUBTRACT},
         {"reverse",     "reverse.png",      0.5, 355,  55, 10, 0, "AltLEFT",   KeyCode.LEFT},
         {"replay",      "replay.png",       0.7, 355, 105, 10, 0, "Alt9",      KeyCode.DIGIT9},
         {"play",        "play.png",         0.7, 400,  10, 20, 0, "Alt]",      KeyCode.CLOSE_BRACKET},
         {"pause",       "pause.png",        0.4, 400,  50, 10, 0, "Alt[",      KeyCode.OPEN_BRACKET},
         {"slow",        "slow.png",         0.7, 400,  90, 20, 0, "Alt\\",     KeyCode.BACK_SLASH},
         {"record",      "record.png",       0.7, 400, 130, 10, 0, "AltR",      KeyCode.R},
         {"thumbsUp",    "thumbsup.png",     0.7, 445,   0, 10, 0, "ADD",       KeyCode.ADD},
         {"forward",     "forward.png",      0.5, 445,  55, 10, 0, "AltRIGHT",  KeyCode.RIGHT},
         {"advance",     "advance.png",      0.7, 445, 105, 10, 0, "Alt0",      KeyCode.DIGIT0},
      };

      for (int i=0; i<Buttons.length; ++i) {
         final String event = (String)Buttons[i][0];
         String imageName = (String)Buttons[i][1];
         double scale = (Double)Buttons[i][2];
         int x = (Integer)Buttons[i][3];
         int y = (Integer)Buttons[i][4];
         int cropx = (Integer)Buttons[i][5];
         int cropy = (Integer)Buttons[i][6];
         String keyName = (String)Buttons[i][7];
         KeyCode keyCode = (KeyCode)Buttons[i][8];
         if (event.startsWith("lab_")) {
            Label l = ImageLabel(imageName, scale);
            if (l == null) continue;
            l.setLayoutX(x);
            l.setLayoutY(y);
            panel_controls.getChildren().add(l);
         } else {
            Button b = ImageButton(imageName, scale);
            if (b == null) continue;
            b.setTooltip(tooltip.getToolTip(event));
            if (event.equals("left"))
               AddButtonShortcut(b, keyName, keyCode);
            if (event.equals("right"))
               AddButtonShortcut(b, keyName, keyCode);
            if (event.equals("up"))
               AddButtonShortcut(b, keyName, keyCode);
            if (event.equals("down"))
               AddButtonShortcut(b, keyName, keyCode);
            panel_controls.getChildren().add(b);
            b.setLayoutX(x);
            b.setLayoutY(y);
            b.setPrefWidth(b.getPrefWidth()-cropx);
            b.setPrefHeight(b.getPrefHeight()-cropy);
            b.setOnAction(new EventHandler<ActionEvent>() {
               public void handle(ActionEvent e) {
                  // Set focus on tabbed_panel
                  Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                        if (config.gui.remote_gui != null)
                           config.gui.remote_gui.tabbed_panel.requestFocus();
                     }
                  });
                  final String tivoName = (String)tivo.getValue();
                  if (tivoName != null && tivoName.length() > 0) {
                     Task<Void> task = new Task<Void>() {
                        @Override public Void call() {
                           if (config.rpcEnabled(tivoName)) {
                              Remote r = config.initRemote(tivoName);
                              if (r.success) {
                                 try {
                                    JSONObject json = new JSONObject();
                                    json.put("event", event);
                                    r.Command("keyEventSend", json);
                                 } catch (JSONException e1) {
                                    log.error("RC - " + e1.getMessage());
                                 }
                                 r.disconnect();
                              }
                           } else {
                              // Use telnet protocol
                              new telnet(config.TIVOS.get(tivoName), mapToTelnet(new String[] {event}));
                           }
                           // Set focus on tabbed_panel
                           Platform.runLater(new Runnable() {
                              @Override
                              public void run() {
                                 config.gui.remote_gui.tabbed_panel.requestFocus();
                              }
                           });
                           return null;
                        }
                     };
                     new Thread(task).start();
                  }
               }
            });
            if (keyName != null && keyCode != null) {
               AddButtonShortcut(b, keyName, keyCode);
            }
         }
      }
      
      // Special buttons
      Button standby = new CustomButton(
         "Toggle standby", "standby",
         new String[] {"standby"}
      );
      panel_controls.getChildren().add(standby);
      standby.setLayoutX(500);
      standby.setLayoutY(10);
      
      Button toggle_cc = new CustomButton("Toggle CC", "toggle_cc", null);
      panel_controls.getChildren().add(toggle_cc);
      toggle_cc.setLayoutX(500);
      toggle_cc.setLayoutY(40);
      toggle_cc.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            String tivoName = (String)tivo.getValue();
            if (tivoName != null && tivoName.length() > 0) {
               String event;
               if (cc_state)
                  event = "ccOff";
               else
                  event = "ccOn";
               cc_state = ! cc_state;
               if (config.rpcEnabled(tivoName)) {
                  Remote r = config.initRemote(tivoName);
                  if (r.success) {
                     try {
                        JSONObject json = new JSONObject();
                        json.put("event", event);
                        r.Command("keyEventSend", json);
                     } catch (JSONException e1) {
                        log.error("RC - " + e1.getMessage());
                     }
                     r.disconnect();
                  }
               } else {
                  // Use telnet interface
                  String[] sequence = new String[1];
                  if (event.equals("ccOff"))
                     sequence[0] = "CC_OFF";
                  if (event.equals("ccOn"))
                     sequence[0] = "CC_ON";
                  new telnet(config.TIVOS.get(tivoName), sequence);                     
               }
            }
         }
      });
      
      Button myShows = new CustomButton("My Shows", "My Shows", null);
      panel_controls.getChildren().add(myShows);
      myShows.setLayoutX(500);
      myShows.setLayoutY(70);
      myShows.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            String tivoName = (String)tivo.getValue();
            if (tivoName != null && tivoName.length() > 0) {
               if (config.rpcEnabled(tivoName)) {
                  Remote r = config.initRemote(tivoName);
                  if (r.success) {
                     try {
                        JSONObject json = new JSONObject();
                        json.put("event", "nowShowing");
                        r.Command("keyEventSend", json);
                     } catch (JSONException e1) {
                        log.error("RC - " + e1.getMessage());
                     }
                     r.disconnect();
                  }
               } else {
                  // Use telnet interface
                  String[] sequence = new String[] {"NOWSHOWING"};
                  new telnet(config.TIVOS.get(tivoName), sequence);                     
               }
            }
         }
      });
      
      Button find_remote = new CustomButton("Find remote", "Find remote", null);
      panel_controls.getChildren().add(find_remote);
      find_remote.setLayoutX(500);
      find_remote.setLayoutY(100);
      find_remote.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            String tivoName = (String)tivo.getValue();
            // Use telnet interface
            log.print("Find remote pressed");
            String[] sequence = new String[] {"FIND_REMOTE"};
            new telnet(config.TIVOS.get(tivoName), sequence);
         }
      });

    Button search_command = new CustomButton("Search...", "Search prompt", null);
    panel_controls.getChildren().add(search_command);
    search_command.setLayoutX(500);
    search_command.setLayoutY(130);
    search_command.setOnAction(new EventHandler<ActionEvent>() {
       public void handle(ActionEvent e) {
          // NOTE JavaFX TextInputDialog requires a particular minimum java version (JDK 1.8.0_40).
          TextInputDialog alert = new TextInputDialog();
          alert.setHeaderText("Enter search to perform with Network Remote Control.");
          alert.showAndWait();
          String search = alert.getResult();
          if (search == null) {
             return;
          }
          String tivoName = (String)tivo.getValue();
          // prepare macro interface
          log.print("Search for: "+search);
          String commands[];
          if(search.length() > 0) {
              commands = new String[search.length()+2];
              int i = 0;
              commands[i++] = "search";
              // search can show up with previous search which would be appended to.
              commands[i++] = "clear";
              for(char c : search.toCharArray()) {
                 commands[i++] = String.valueOf(c);
              }
          } else {
             commands = new String[] {"search"};
          }
          // 200 still wasn't enough interval - some keys were being lost.
          executeMacro(tivoName, commands, 300);
//          new telnet(config.TIVOS.get(tivoName), commands, 300);
       }
    });
//      Button telnet_command = new CustomButton("IRCODE...", "IRCODE...", null);
//      panel_controls.getChildren().add(telnet_command);
//      telnet_command.setLayoutX(500);
//      telnet_command.setLayoutY(130);
//      telnet_command.setOnAction(new EventHandler<ActionEvent>() {
//         public void handle(ActionEvent e) {
//        	 TextInputDialog alert = new TextInputDialog("IRCODE TIVO");
//        	 alert.setHeaderText("telnet command to try (e.g. IRCODE NETFLIX; TELEPORT LIVETV; KEYBOARD SEMICOLON):"); //, "Delete " + selection + " ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
//        	 alert.showAndWait();
//        	 String command = alert.getResult();
//        	 if (command == null) {
//        		 return;
//        	 }     	 
//            String tivoName = (String)tivo.getValue();
//            // Use telnet interface
//            log.print("telnet command: "+command);
//            new telnet(config.TIVOS.get(tivoName), command);
//         }
//      });

      // Other components for the panel      
      Label label = new Label("TiVo");
      label.setStyle("-fx-text-fill: " + text_color + ";");

      tivo = new ChoiceBox<String>();
      tivo.valueProperty().addListener(new ChangeListener<String>() {
         @Override public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
            if (newVal != null && config.gui.remote_gui != null) {
                String tivoName = newVal;
                config.gui.remote_gui.updateButtonStates(tivoName, "Remote");
            }
         }
      });
      tivo.setTooltip(tooltip.getToolTip("tivo_rc"));

      hme_button = new Button("Launch App:");
      disableSpaceAction(hme_button);
      hme_button.setTooltip(tooltip.getToolTip("hme_button"));
      hme_button.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            final String name = hme.getValue();
            if (name != null && name.length() > 0) {
               Task<Void> task = new Task<Void>() {
                  @Override public Void call() {
                     Remote r = config.initRemote(tivo.getValue());
                     if (r.success) {
                        LinkedHashMap<String, String> apps = getAppData();
                        String uri = apps.get(name);
                        r.navigate(uri);

                        r.disconnect();
                     }
                     return null;
                  }
               };
               new Thread(task).start();
            }
         }
      });
      
      hme = new ChoiceBox<String>();
      hme.setTooltip(tooltip.getToolTip("hme_rc"));

      // util.SPS backdoors
      String sps_name, sps_text;
      String sps_text_end = "Should be used while playing back a recorded show.";
      sps_name = "Quick clear play bar: SPSPS";
      util.SPS.put(sps_name, "select play select pause select play");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Toggle 'clear trickplay banner quickly' setting.<br>";
      sps_text += "This will also clear any 'pause ads' quickly.<br>";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);

      sps_name = "Clock: SPS9S";
      util.SPS.put(sps_name, "select play select 9 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Toggle on screen clock.<br>";
      sps_text += "Clock will be at top right corner for series 4 TiVos or later.<br>";
      sps_text += "Clock will be at bottom right corner for series 3 TiVos.<br>";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
            
      sps_name = "30 sec skip: SPS30S";
      util.SPS.put(sps_name, "select play select 3 0 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Toggle 30 sec skip binding of advance button.<br>";
      sps_text += "NOTE: Unlike other backdoors, this one survives a reboot.<br>";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "Information: SPSRS";
      util.SPS.put(sps_name, "select play select replay select");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Display some video information on the screen.<br>";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "Calibration: SPS7S";
      util.SPS.put(sps_name, "select play select 7 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Display calibration map for centering and overscan.<br>";
      sps_text += "NOTE: This only works for series 3 TiVos.<br>";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "4x FF: SPS88S";
      util.SPS.put(sps_name, "select play select 8 8 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Toggles '4th FF press returns to play speed' setting.<br>";
      sps_text += "Series 4 software changed behavior such that beyond 3 FF presses nothing happens.<br>";
      sps_text += "When enabled a 4th FF press resumes normal play as was the case with older TiVo software.<br>";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.1x quickplay: SPS71S";
      util.SPS.put(sps_name, "select play select 7 1 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.1x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.2x quickplay: SPS72S";
      util.SPS.put(sps_name, "select play select 7 2 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.2x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.3x quickplay: SPS73S";
      util.SPS.put(sps_name, "select play select 7 3 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.3x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.4x quickplay: SPS74S";
      util.SPS.put(sps_name, "select play select 7 4 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.4x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.5x quickplay: SPS75S";
      util.SPS.put(sps_name, "select play select 7 5 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.5x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.6x quickplay: SPS76S";
      util.SPS.put(sps_name, "select play select 7 6 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.6x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.7x quickplay: SPS77S";
      util.SPS.put(sps_name, "select play select 7 7 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.7x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.8x quickplay: SPS78S";
      util.SPS.put(sps_name, "select play select 8 1 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.8x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
      
      sps_name = "1.9x quickplay: SPS79S";
      util.SPS.put(sps_name, "select play select 7 9 select clear");
      sps_text = "<b>" + sps_name + "</b><br>";
      sps_text += util.SPS.get(sps_name) + "<br>";
      sps_text += "Sets quickplay speed to 1.9x speed.<br>";
      sps_text += "Only supported for series 5 and later models with quickplay.";
      sps_text += sps_text_end;
      util.SPS.put(sps_name + "_tooltip", sps_text);
     
      sps_button = new Button("SPS backdoor:");
      disableSpaceAction(sps_button);
      sps_button.setTooltip(tooltip.getToolTip("rc_sps_button"));
      sps_button.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            String name = (String)hme_sps.getValue();
            String tivoName = (String)tivo.getValue();
            if (name != null && name.length() > 0 && tivoName != null && tivoName.length() > 0) {
               executeMacro(
                  tivoName,
                  util.SPS.get(name).split(" ")
               );
            }
         }
      });
      
      hme_sps = new ChoiceBox<String>();
      hme_sps.valueProperty().addListener(new ChangeListener<String>() {
         @Override public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
            if (newVal != null) {
               hme_sps.setTooltip(tooltip.getToolTip(newVal));            
            }
         }
      });
      for (String name : util.SPS.keySet()) {
         if (! name.contains("_tooltip")) {
            hme_sps.getItems().add(name);
         }
         hme_sps.getSelectionModel().select(0);
      }
      
      jumpto_button = new Button("Jump to minute:");
      AddButtonShortcut(jumpto_button, "Altm", KeyCode.M);
      disableSpaceAction(jumpto_button);
      jumpto_button.setTooltip(tooltip.getToolTip("jumpto_text"));
      jumpto_button.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            final String tivoName = (String)tivo.getValue();
            String mins_string = string.removeLeadingTrailingSpaces(jumpto_text.getText());
            if (tivoName == null || tivoName.length() == 0)
               return;
            if (mins_string == null || mins_string.length() == 0)
               return;
            try {
               final int secs = (int)(Float.parseFloat(mins_string)*60);
               Task<Void> task = new Task<Void>() {
                  @Override public Void call() {
                     Remote r = config.initRemote(tivoName);
                     if (r.success) {
                        JSONObject json = new JSONObject();
                        try {
                           Long pos = (long)1000*secs;
                           json.put("offset", pos);
                           r.Command("Jump", json);
                        } catch (JSONException e) {
                           log.error("Jump to minute failed - " + e.getMessage());
                        }
                        r.disconnect();
                     }
                     return null;
                  }
               };
               new Thread(task).start();
            } catch (NumberFormatException e1) {
               log.error("Illegal number of minutes specified: " + mins_string);
               return;
            }            
         }
      });
      jumpto_text = new TextField(); jumpto_text.setMinWidth(50); jumpto_text.setPrefWidth(50);
      jumpto_text.setTooltip(tooltip.getToolTip("jumpto_text"));
      jumpto_text.setText("0");

      jumpahead_button = new Button("Skip minutes ahead:");
      AddButtonShortcut(jumpahead_button, "Alt.", KeyCode.PERIOD);
      disableSpaceAction(jumpahead_button);
      jumpahead_button.setTooltip(tooltip.getToolTip("jumpahead_text"));
      jumpahead_button.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            final String tivoName = (String)tivo.getValue();
            String mins_string = string.removeLeadingTrailingSpaces(jumpahead_text.getText());
            if (tivoName == null || tivoName.length() == 0)
               return;
            if (mins_string == null || mins_string.length() == 0)
               return;
            try {
               final int secs = (int)(Float.parseFloat(mins_string)*60);
               Task<Void> task = new Task<Void>() {
                  @Override public Void call() {
                     Remote r = config.initRemote(tivoName);
                     if (r.success) {
                        JSONObject json = new JSONObject();
                        JSONObject reply = r.Command("Position", json);
                        if (reply != null && reply.has("position")) {
                           try {
                              Long pos = reply.getLong("position");
                              pos += (long)1000*secs;
                              json.put("offset", pos);
                              r.Command("Jump", json);
                           } catch (JSONException e) {
                              log.error("Skip minutes ahead failed - " + e.getMessage());
                           }
                        }
                        r.disconnect();
                     }
                     return null;
                  }
               };
               new Thread(task).start();
            } catch (NumberFormatException e1) {
               log.error("Illegal number of minutes specified: " + mins_string);
               return;
            }            
         }
      });
      jumpahead_text = new TextField(); jumpahead_text.setMinWidth(50); jumpahead_text.setPrefWidth(50);
      jumpahead_text.setTooltip(tooltip.getToolTip("jumpahead_text"));
      jumpahead_text.setText("5");

      jumpback_button = new Button("Skip minutes back:");
      AddButtonShortcut(jumpback_button, "Alt,", KeyCode.COMMA);
      disableSpaceAction(jumpback_button);
      jumpback_button.setTooltip(tooltip.getToolTip("jumpback_text"));
      jumpback_button.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            final String tivoName = (String)tivo.getValue();
            String mins_string = string.removeLeadingTrailingSpaces(jumpback_text.getText());
            if (tivoName == null || tivoName.length() == 0)
               return;
            if (mins_string == null || mins_string.length() == 0)
               return;
            try {
               final int secs = (int)(Float.parseFloat(mins_string)*60);
               Task<Void> task = new Task<Void>() {
                  @Override public Void call() {
                     Remote r = config.initRemote(tivoName);
                     if (r.success) {
                        JSONObject json = new JSONObject();
                        JSONObject reply = r.Command("Position", json);
                        if (reply != null && reply.has("position")) {
                           try {
                              Long pos = reply.getLong("position");
                              pos -= (long)1000*secs;
                              if (pos < 0)
                                 pos = (long)0;
                              json.put("offset", pos);
                              r.Command("Jump", json);
                           } catch (JSONException e) {
                              log.error("Skip minutes back failed - " + e.getMessage());
                           }
                        }
                        r.disconnect();
                     }
                     return null;
                  }
               };
               new Thread(task).start();
            } catch (NumberFormatException e1) {
               log.error("Illegal number of minutes specified: " + mins_string);
               return;
            }            
         }
      });
      jumpback_text = new TextField(); jumpback_text.setMinWidth(50); jumpback_text.setPrefWidth(50);
      jumpback_text.setTooltip(tooltip.getToolTip("jumpback_text"));
      jumpback_text.setText("5");
      
      // Top panel
      HBox rctop = new HBox();
      rctop.setSpacing(5);
      rctop.setPadding(new Insets(0,0,0,5));
      rctop.setAlignment(Pos.CENTER_LEFT);
      rctop.getChildren().add(label);
      rctop.getChildren().add(tivo);
      rctop.getChildren().add(hme_button);
      rctop.getChildren().add(hme);
      rctop.getChildren().add(sps_button);
      rctop.getChildren().add(hme_sps);

      // Bottom panel
      HBox rcbot = new HBox();
      rcbot.setSpacing(5);
      rcbot.setPadding(new Insets(0,0,0,5));
      rcbot.setAlignment(Pos.CENTER_LEFT);
      rcbot.getChildren().add(jumpto_button);
      rcbot.getChildren().add(jumpto_text);
      rcbot.getChildren().add(jumpback_button);
      rcbot.getChildren().add(jumpback_text);
      rcbot.getChildren().add(jumpahead_button);
      rcbot.getChildren().add(jumpahead_text);

      // Combine all RC panels together
      panel.setAlignment(Pos.CENTER);
      panel.getChildren().addAll(rctop, panel_controls, rcbot);
      
      // RC tab keyboard shortcuts without buttons
      for (char c='A'; c<='Z'; ++c) {
         AddPanelShortcut("" + c, KeyCode.getKeyCode("" + c), true, new String("" + Character.toChars(c)[0]).toLowerCase());
         AddPanelShortcut("Shift" + c, KeyCode.getKeyCode("" + c), true, "" + Character.toChars(c)[0]);
      }
      // NOTE: The special chars I copied from slide remote (Sym chars)
      Object[][] kb_shortcuts = new Object[][] {
         // NAME       Keyboard KeyEvent   isAscii action
         {"SPACE",          KeyCode.SPACE,      false, "forward"},
         {"BACKSPACE",      KeyCode.BACK_SPACE, false, "reverse"},
         {"PERIOD",         KeyCode.PERIOD,     true,  "."},
         {"QUOTE",          KeyCode.QUOTE,      true,  "'"},
         {"NUMPAD0",        KeyCode.NUMPAD0,    true,  "0"},
         {"NUMPAD1",        KeyCode.NUMPAD1,    true,  "1"},
         {"NUMPAD2",        KeyCode.NUMPAD2,    true,  "2"},
         {"NUMPAD3",        KeyCode.NUMPAD3,    true,  "3"},
         {"NUMPAD4",        KeyCode.NUMPAD4,    true,  "4"},
         {"NUMPAD5",        KeyCode.NUMPAD5,    true,  "5"},
         {"NUMPAD6",        KeyCode.NUMPAD6,    true,  "6"},
         {"NUMPAD7",        KeyCode.NUMPAD7,    true,  "7"},
         {"NUMPAD8",        KeyCode.NUMPAD8,    true,  "8"},
         {"NUMPAD9",        KeyCode.NUMPAD9,    true,  "9"},
         {"SEMICOLON",      KeyCode.SEMICOLON,  true,  ";"},
         {"BACKQUOTE",      KeyCode.BACK_QUOTE, true,  "`"},
         {"MINUS",          KeyCode.MINUS,      true,  "-"},
         {"EQUALS",         KeyCode.EQUALS,     true,  "="},
         {"OPEN_BRACKET",   KeyCode.OPEN_BRACKET, true,  "["},
         {"CLOSE_BRACKET",  KeyCode.CLOSE_BRACKET, true,  "]"},
         {"COMMA",          KeyCode.COMMA,      true,  ","},
         {"QUOTE",          KeyCode.QUOTE,      true,  "'"},
         {"SLASH",          KeyCode.SLASH,      true,  "/"},
         {"BACKSLASH",      KeyCode.BACK_SLASH, true,  "\\"},
         {"Shift1",         KeyCode.DIGIT1,     true,  "!"},
         {"Shift2",         KeyCode.DIGIT2,     true,  "@"},
         {"Shift3",         KeyCode.DIGIT3,     true,  "#"},
         {"Shift4",         KeyCode.DIGIT4,     true,  "$"},
         {"Shift5",         KeyCode.DIGIT5,     true,  "%"},
         {"Shift6",         KeyCode.DIGIT6,     true,  "^"},
         {"Shift7",         KeyCode.DIGIT7,     true,  "&"},
         {"Shift8",         KeyCode.DIGIT8,     true,  "*"},
         {"Shift9",         KeyCode.DIGIT9,     true,  "("},
         {"Shift0",         KeyCode.DIGIT0,     true,  ")"},
         {"ShiftBACKQUOTE", KeyCode.BACK_QUOTE, true,  "~"},
         {"ShiftMINUS",     KeyCode.MINUS,      true,  "_"},
         {"ShiftEQUALS",    KeyCode.EQUALS,     true,  "+"},
         {"ShiftOPEN_BRACKET", KeyCode.OPEN_BRACKET,  true, "{"},
         {"ShiftCLOSE_BRACKET", KeyCode.CLOSE_BRACKET, true, "}"},
         {"ShiftSEMICOLON", KeyCode.SEMICOLON,  true,  ":"},
         {"ShiftSLASH",     KeyCode.SLASH,      true,  "?"},
         {"ShiftQUOTE",     KeyCode.QUOTE,      true,  "\""},
         {"ShiftCOMMA",     KeyCode.COMMA,      true,  "<"},
         {"ShiftPERIOD",    KeyCode.PERIOD,     true,  ">"},
         {"ShiftBACKSLASH", KeyCode.BACK_SLASH, true,  "|"},
      };
      for (int i=0; i<kb_shortcuts.length; ++i) {
         AddPanelShortcut(
            (String)kb_shortcuts[i][0],
            (KeyCode)kb_shortcuts[i][1],
            (Boolean)kb_shortcuts[i][2],
            (String)kb_shortcuts[i][3]
         );
      }      
   }

   private class CustomButton extends Button {
      private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 5, 5, 5, 5;";
      private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 6 4 4 6;";
      private final String STYLE_LABEL1 = "-fx-background-color: " + background +
            "; -fx-text-fill: " + text_color + "; -fx-padding: 5, 5, 5, 5;";
      private final String STYLE_LABEL2 = "-fx-background-color: " + background +
            "; -fx-text-fill: " + text_color + "; -fx-padding: 6 4 4 6;";
      
      public CustomButton(Image image) {
         super();
         setGraphic(new ImageView(image));
         setStyle(STYLE_NORMAL);
         
         // These actions give visual effect when button pressed
         setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setStyle(STYLE_PRESSED);
            }            
         });
        
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               setStyle(STYLE_NORMAL);
            }            
         });
      }
      
      public CustomButton(String label, String toolTipKey, String[] macro) {
         super(label);
         setTooltip(tooltip.getToolTip(toolTipKey));
         if (macro != null)
            setMacroCB(this, macro);
         setStyle(STYLE_LABEL1);
         
         // These actions give visual effect when button pressed
         setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setStyle(STYLE_LABEL2);
            }            
         });
        
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               setStyle(STYLE_LABEL1);
            }            
         });
      }
   }
   
   private static Image scale(String imageFile, double scale) {
      Image unscaled = new Image(new File(imageFile).toURI().toString());
      if (scale < 1.0) {
         return new Image(
            new File(imageFile).toURI().toString(),
            scale*unscaled.getWidth(),
            scale*unscaled.getHeight(), false, false
         );
      } else {
         return unscaled;
      }
   }
   
   private Button ImageButton(String imageFile, double scale) {
      String f = config.programDir + File.separator + "rc_images" + File.separator + imageFile;
      if (file.isFile(f)) {
         Button b = new CustomButton(scale(f, scale));
         disableSpaceAction(b);
         return b;
      }
      log.error("Installation issue: image file not found: " + f);
      return null;
   }

   private Label ImageLabel(String imageFile, double scale) {
      String f = config.programDir + File.separator + "rc_images" + File.separator + imageFile;
      if (file.isFile(f)) {
         ImageView view = new ImageView(scale(f, scale));
         Label l = new Label();
         l.setGraphic(view);
         l.setStyle("-fx-background-color:" + background);
         return l;
      }
      log.error("Installation issue: image file not found: " + f);
      return null;
   }
   
   private void AddButtonShortcut(Button b, String actionName, KeyCode key) {
      PanelKey.buttonKeys.push(new PanelKey(actionName, key, b));
   }
   
   private void disableSpaceAction(Button b) {
      b.setOnKeyPressed(new EventHandler<KeyEvent> () {
         @Override
         public void handle(KeyEvent event) {
            event.consume();
         }
         
      });
   }
   
   private void AddPanelShortcut(String actionName, KeyCode key, Boolean isAscii, String command) {
      PanelKey.panelKeys.push(new PanelKey(actionName, key, isAscii, command));
   }

   private String[] mapToTelnet(String[] sequence) {
      Stack<String> n = new Stack<String>();
      for (int i=0; i<sequence.length; ++i) {
         String u = sequence[i].toUpperCase();
         if (! u.startsWith("ACTION")) {
            if (u.equals("ZOOM"))
               u = "WINDOW";
            if (u.equals("BACK"))
               u = "REPLAY";
            n.add(u);
         }
      }
      String[] mapped = new String[n.size()];
      for (int i=0; i<n.size(); ++i)
         mapped[i] = n.get(i);
      return mapped;
   }

   private void executeMacro(final String tivoName, final String[] sequence) {
      executeMacro(tivoName, sequence, telnet.DEFAULT_BUTTON_INTERVAL);
   }
   private void executeMacro(final String tivoName, final String[] sequence, final int telnetInterval) {
      Task<Void> task = new Task<Void>() {
         @Override public Void call() {
            if (config.rpcEnabled(tivoName)) {
               Remote r = config.initRemote(tivoName);
               String[] seq;
               if(sequence.length > 0 && sequence[0].equals("search")) {
                  r.navigate("x-tivo:flash:tivo_hdui?screenName=search");
                  seq = (String[]) Arrays.copyOfRange(sequence, 1, sequence.length);
               } else {
                  seq = sequence;
               }
               if(seq.length > 0) {
                  r.keyEventMacro(seq);
               }
            } else {
               // Use telnet protocol
               new telnet(config.TIVOS.get(tivoName), mapToTelnet(sequence), telnetInterval);
            }
            // Set focus on tabbed_panel
            Platform.runLater(new Runnable() {
               @Override
               public void run() {
                  config.gui.remote_gui.tabbed_panel.requestFocus();
               }
            });
            return null;
         }
      };
      new Thread(task).start();
   }

   private void setMacroCB(Button b, final String[] sequence) {
      b.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent e) {
            // Set focus on tabbed_panel
            Platform.runLater(new Runnable() {
               @Override
               public void run() {
                  config.gui.remote_gui.tabbed_panel.requestFocus();
               }
            });
            final String tivoName = tivo.getValue();
            if (tivoName != null && tivoName.length() > 0) {
               executeMacro(tivoName, sequence);
            }
         }
      });
   }

   // This handles key presses in RC panel not bound to buttons
   public void RC_keyPress(final Boolean isAscii, final String command) {
      Task<Void> task = new Task<Void>() {
         @Override public Void call() {
            String tivoName = tivo.getValue();
            if (tivoName != null && tivoName.length() > 0) {
               if (config.rpcEnabled(tivoName)) {
                  Remote r = config.initRemote(tivoName);
                  if (r.success) {
                     try {
                        JSONObject json = new JSONObject();
                        if (isAscii) {
                           json.put("event", "text");
                           r.Command("keyEventSend", json);
                           json.put("event", "ascii");
                           json.put("value", command.toCharArray()[0]);
                        }
                        else {
                           json.put("event", command);
                        }
                        r.Command("keyEventSend", json);
                     } catch (JSONException e1) {
                        log.error("RC keyPressed - " + e1.getMessage());
                     }
                     r.disconnect();
                  }
               } else {
                  // Use telnet protocol
                  if (isAscii)
                     new telnet(config.TIVOS.get(tivoName), new String[] {command});
                  else
                     new telnet(config.TIVOS.get(tivoName), mapToTelnet(new String[] {command}));
               }
            }
            return null;
         }
      };
      new Thread(task).start();
   }
      
   /**
    * Read the web/rc_apps.json file as a JSONArray
    * @return
    */
   public static JSONArray readAppConfiguration() {
      String filename = "rc_apps.json";
      String webdir = config.httpserver_home;
      File file = new File(webdir + File.separator + filename);
      
      // data for default rc_apps.json with no disabled items
      JSONArray default_apps = new JSONArray();
      String[][] data = new String[][] {
//         new String[] {"Tivos ToGether: KMTTG HME", "x-tivo:hme:http://localhost:7291/ttg", 
//               "0-0", "HME App interface to some features of KMTTG, if available"},
         new String[] {"Netflix (html)", "x-tivo:netflix:netflix", 
               "0-1", "Telnet remote interface can do IRCODE NETFLIX. TiVo Premiere Q & Suddenlink: channel 3000, eleven other launch channels used by different lineups per https://help.netflix.com/en/node/23925"},
         new String[] {"YouTube (html)", "x-tivo:web:https://www.youtube.com/tv", 
               null, ""},
         new String[] {"Vudu (html)", "x-tivo:vudu:vudu", 
               "0-4", ""},
         new String[] {"Plex", "x-tivo:web:https://plex.tv/web/tv/tivo", 
               "0-3", ""},
         new String[] {"Amazon Prime", "x-tivo:web:https://atv-ext.amazon.com/cdp/resources/app_host/index.html?deviceTypeID=A3UXGKN0EORVOF", 
               null, ""},
         new String[] {"Hulu Plus", "x-tivo:flash:uuid:802897EB-D16B-40C8-AEEF-0CCADB480559",
               "0-2", "TiVo Premiere Q: channel 3001, Shentel: channel 3000"},
         new String[] {"Spotify", "x-tivo:web:https://d27nv3bwly96dm.cloudfront.net/indexOperav2.html",
               "1-2", ""},
         new String[] {"iHeartRadio", "x-tivo:web:https://tv.iheart.com/tivo/",
               "1-3", ""},
         new String[] {"Launchpad", "x-tivo:flash:uuid:545E064D-C899-407E-9814-69A021D68DAD", 
               null, "2.4l lists only on web"},
         new String[] {"Opera TV Store", "x-tivo:web:tvstore", 
               null, "Telnet remote interface can do IRCODE TVSTORE"},
         new String[] {"streambaby", "x-tivo:hme:http://localhost:7290/streambaby",
               "0-5", "(localhost in uri will be replaced by local ip address)"},
         new String[] {"Archive On Demand", "x-tivo:hme:http://66.193.212.44:7291/archiveorg",
               "0-6", "enterwebz.tv public domain videos HME app"},
      };
      for(String[] d : data) {
         String[] headings = new String[] {"name", "uri", "channel", "description"};
         try {
            JSONObject entry = new JSONObject();
            for(int i = 0 ; i < headings.length ; ++i) {
               if(d.length > i && d[i] != null && d[i].length() > 0)
                  entry.put(headings[i], d[i]);
            }
            default_apps.put(entry);
         } catch (Exception e) {
            log.error("readAppConfiguration - " + e.getMessage());
         }
      }
      
      // generate a default rc_apps.json
      if (!file.exists()) {
         try {
            BufferedWriter os = new BufferedWriter(new FileWriter(file));
            os.write(default_apps.toString(2));
            os.close();
         } catch (Exception e) {
            log.error("readAppConfiguration - " + e.getMessage());
            // at least use defaults, better than null
            return default_apps;
         }
      }
      
      JSONArray rc_apps = new JSONArray();
      try {
         BufferedReader is = new BufferedReader(new FileReader(file));
         rc_apps = new JSONArray(new JSONTokener(is));
         is.close();
      } catch (Exception e) {
         log.error("readAppConfiguration - " + e.getMessage());
         // at least use defaults, better than null
         return default_apps;
      }
      return rc_apps;
   }
   
   private LinkedHashMap<String, String> _appData = null;
   /** get an ordered map of app names to uris from the rc_apps.json file*/
   public LinkedHashMap<String, String> getAppData() {
       if (_appData == null) {
    	   JSONArray rc_apps = readAppConfiguration();

           LinkedHashMap<String, String> data = new LinkedHashMap<String, String>(rc_apps.length());
           
           for(int i = 0 ; i < rc_apps.length() ; ++i) {
        	   try {
	        	   JSONObject app = (JSONObject) rc_apps.get(i);
	        	   // not explicitly disabled, and has name & uri parameters (add to name if it has a channel)
	        	   if(!(app.has("disabled") && app.getBoolean("disabled"))) {
	        		   String add = "";
	        		   if(app.has("channel")) {
	        			   add = " (channel "+app.getString("channel")+")";
	        		   }
	        		   if(app.has("name") && app.has("uri")) {
	        			   data.put(app.getString("name")+add, app.getString("uri"));
	        		   }
	        		   // if(app.has("description"))...
	        	   }
     	      } catch (Exception e) {
     	         log.error("getAppData - " + e.getMessage());
     	      }
           }
           _appData = data;
       }
       return _appData;
   }
      
   public void setHmeDestinations(final String tivoName) {
      LinkedHashMap<String, String> data = getAppData();

      String[] hmeNames = new String[data.size()];
      hmeNames = data.keySet().toArray(hmeNames);
      
      hme.getItems().clear();
      for (int i=0; i<hmeNames.length; ++i)
         hme.getItems().add(hmeNames[i]);
      hme.getSelectionModel().select(hmeNames[0]);
   }

}
