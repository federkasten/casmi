/*
 *   casmi
 *   http://casmi.github.com/
 *   Copyright (C) 2011, Xcoo, Inc.
 *
 *  casmi is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package casmi.timeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GL2;

import casmi.Applet;
import casmi.Keyboard;
import casmi.Mouse;
import casmi.PopupMenu;
import casmi.graphics.Graphics;
import casmi.graphics.element.Reset;
import casmi.parser.CSV;

import java.util.HashMap;  
import java.util.Map;  

/**
 * Timeline class.You can use time line with Scene class.
 * This class controls Scene and inserts disolve effects.
 * 
 * @author Y. Ban
 * 
 * @see Scene
 */
public class Timeline implements TimelineRender, Reset {

    private int nowSceneID = 0, nextSceneID = 1, nowId = 0;
    private double preDhalf = 0.0, nextDhalf = 0.0;
    private boolean endScene = false;
    private boolean dissolve = false, nowDissolve = false;
    private long dissolveStart, dissolveNow;
    private Applet baseApplet;
    private double nowDissolveTime;

    private List<Scene> sceneList;
    private List<Scene> tmpSceneList;
    private Timer timer;
    private TimerTask task = new SceneTask();
    
    private Mouse mouse;
    private Keyboard keyboard;
    private PopupMenu popup;
    private boolean firstCallback = true;

    private Map<String, Integer> map = new HashMap<String, Integer>();
    
    class SceneTask extends TimerTask {

        @Override
        public void run() {
            if (!sceneList.get(nowSceneID).isHasDissolve()) {
                goNextSceneWithCallback();
                
            } else {
                if (!dissolve) {
                    goDissolve();
                } else {
                    sceneList.get(nowSceneID).ExitedSceneCallback();
                    endDisolve();
                }
            }
        }
    }

    public Timeline() {
        sceneList = new ArrayList<Scene>();
        tmpSceneList = new ArrayList<Scene>();
    }


    public final void readTimelineCSV(String csvfile) {
        CSV csv;
        String name;
		double time;
        try {
            csv = new CSV(csvfile);
            String[] test;
            while ((test = csv.readLine()) != null) {
                name = test[0];
                time = Double.valueOf(test[1]).doubleValue();
               {
            	   for(Scene s : tmpSceneList){
            		   if(s.getIdName() == name){
            			   s.setTime(time);
            			   this.appendScene(s);
            			   break;
            		   }
            	   }
               }
            	   
            }
            csv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goNextScene() {
    	firstCallback = true;
        nowSceneID = nextSceneID;
        nextSceneID++;
        try {
            sceneList.get(nextSceneID);
        } catch (java.lang.IndexOutOfBoundsException e) {
            nextSceneID = 0;
        }
        setEndScene(true);
        
    	if(!sceneList.get(nowSceneID).isHasDissolve())
    		nextDhalf = 0;
    	else
    		nextDhalf = sceneList.get(nowSceneID).getDissolve().getTime() / 2.0;

        task.cancel();
        task = null;
        task = new SceneTask();
        if(sceneList.get(nowSceneID).getTime()>0)
        	timer.schedule(task, (long)(1000*((sceneList.get(nowSceneID).getTime() - preDhalf - nextDhalf))));
        preDhalf = 0;
        nextDhalf = 0;
    }

    private final void endDisolve() {
    	if(!sceneList.get(nowSceneID).isHasDissolve())
    		preDhalf = 0;
    	else
    		preDhalf = sceneList.get(nowSceneID).getDissolve().getTime() / 2.0;

        dissolve = false;

        goNextScene();

    }

    private final void goDissolve() {
        dissolve = true;
        setEndScene(true);

        task.cancel();
        task = null;
        task = new SceneTask();
        timer.schedule(task, (long)(1000*(sceneList.get(nowSceneID).getDissolve().getTime())));
        if(sceneList.get(nowSceneID).getDissolve().getMode()==DissolveMode.CROSS)
        	sceneList.get(nextSceneID).EnteredSceneCallback();
    }

    public void goNextSceneWithCallback() {
        sceneList.get(nowSceneID).ExitedSceneCallback();
        sceneList.get(nextSceneID).EnteredSceneCallback();
        goNextScene();
    }
    
    public void goNextScene(String idName) {
        nextSceneID = this.map.get(idName);
        try {
            sceneList.get(nextSceneID);
        } catch (java.lang.IndexOutOfBoundsException e) {
            nextSceneID = nowSceneID;
        }
        if(!sceneList.get(nowSceneID).isHasDissolve())
        	goNextSceneWithCallback();
        else
        	goDissolve();
    }
    
    public void goNextScene(String idName, DissolveMode mode, double time) {
        nextSceneID = this.map.get(idName);
        try {
            sceneList.get(nextSceneID);
        } catch (java.lang.IndexOutOfBoundsException e) {
            nextSceneID = nowSceneID;
        }
        sceneList.get(nowSceneID).setDissolve(new Dissolve(mode, time));
        sceneList.get(nowSceneID).setHasDissolve(true);
        goDissolve();
    }

    public void appendScene(Scene s) {
    	s.setRootTimeline(this);
        this.sceneList.add(s);
        this.map.put(s.getIdName(), this.sceneList.size()-1);
    }
    
    public void appendScene(Scene s, DissolveMode mode, double dissolveTime) {
    	s.setRootTimeline(this);
        this.sceneList.add(s);
        this.map.put(s.getIdName(), this.sceneList.size()-1);
        s.setDissolve(new Dissolve(mode, dissolveTime));
        s.setHasDissolve(true);
    }

    public void removeScene(int i) {
        this.sceneList.remove(i);
    }
    
    public void removeScene(String idName) {
    	this.sceneList.remove(this.map.get(idName));
    }

    public void startTimer() {
        timer = new Timer(true);
        double halfd = 0;
        try {
            if (sceneList.get(nowSceneID).isHasDissolve()) 
                halfd = (sceneList.get(nowSceneID).getDissolve().getTime() / 2.0);
            if(sceneList.get(nowSceneID).getTime()>0)
            	timer.schedule(task, (long)(1000*(sceneList.get(nowSceneID).getTime() - halfd)));
            
        } catch (java.lang.IndexOutOfBoundsException e) {
        	if(sceneList.get(nowSceneID).getTime()>0)
        		timer.schedule(task, (long)(1000*(sceneList.get(nowSceneID).getTime())));
        }
    }

    public int render(Graphics g) {

        if (!dissolve) {
            sceneList.get(nowSceneID).drawscene(g);
            dissolveStart = System.currentTimeMillis();
            
        } else {
        	nowDissolve = true;
            dissolveNow = System.currentTimeMillis();
            nowDissolveTime = 0;
            if(sceneList.get(nowSceneID).isHasDissolve())
            	nowDissolveTime = (dissolveNow - dissolveStart) / ((sceneList.get(nowSceneID).getDissolve().getTime() * 1000));
            else
            	return 0;
            if(nowDissolveTime>=0.5)
            	nowId = nextSceneID;
            else 
            	nowId = nowSceneID;
            if(nowDissolveTime>1.0){
            	nowDissolve = false;
            	nowDissolveTime = 1.0;
            }
            if(nowId==nextSceneID && sceneList.get(nowSceneID).getDissolve().getMode()==DissolveMode.BLACK && firstCallback){
            	sceneList.get(nextSceneID).EnteredSceneCallback();
            	firstCallback = false;
            }
            
            switch (sceneList.get(nowSceneID).getDissolve().getMode()) {
            default:
            case CROSS:
            	sceneList.get(nowSceneID).setDepthTest(false);
                sceneList.get(nowSceneID).setSceneA((1.0 - nowDissolveTime), g);
                if(nowDissolve)
                	sceneList.get(nowSceneID).drawscene(g);
                sceneList.get(nextSceneID).setSceneA(nowDissolveTime, g);
                sceneList.get(nextSceneID).drawscene(g);
                break;
            case BLACK:
                if (nowDissolveTime <= 0.5) {
                    sceneList.get(nowSceneID).setSceneA((1.0 - nowDissolveTime * 2), g);
                    sceneList.get(nowSceneID).drawscene(g);
                }
                if (nowDissolveTime >= 0.5) {
                    sceneList.get(nextSceneID).setSceneA(((nowDissolveTime - 0.5) * 2), g);
                    sceneList.get(nextSceneID).drawscene(g);
                }
                break;
            }
        }
        return 0;
    }

    public boolean isEndScene() {
        return endScene;
    }

    public Scene getScene() {
        return sceneList.get(nowId);
    }

    public Scene getScene(int index) {
        return sceneList.get(index);
    }

    public void setApplet(Applet a) {
        baseApplet = a;
    }

    public Applet getApplet() {
        return baseApplet;
    }

    public boolean setEndScene(boolean endScene) {
        this.endScene = endScene;
        return endScene;
    }
    
    public void setKeyboard(Keyboard keyboard){
    	this.keyboard = keyboard;
    }
    
    public void setMouse(Mouse mouse){
    	this.mouse = mouse;
    }
    
    public void setPopup(PopupMenu popup){
    	this.popup = popup;
    }
    
    protected Keyboard getKeyboard(){
    	return this.keyboard;
    }
    
    public Mouse getMouse(){
    	return this.mouse;
    }
    
    public PopupMenu getPopup(){
    	return this.popup;
    }
    
    public boolean isNowDissolve() {
    	return nowDissolve;
    }


	@Override
	public void reset(GL2 gl) {
		this.getScene().reset(gl);
		
	}


	public void setSize(int width, int height) {
		this.getApplet().setSize(width, height);
	}


	public int getWidth() {
		return this.getApplet().getWidth();
	}
	
	public int getHeight() {
		return this.getApplet().getHeight();
	}
}
