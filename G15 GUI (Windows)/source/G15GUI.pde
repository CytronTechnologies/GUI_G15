import processing.serial.*;
import g4p_controls.*;

int SerialTimeOut=100000; 
int[] baudrates={9600, 19200, 38400, 57600, 115200, 500000};
int serialflag=0; 
boolean tx =false; 
boolean rx =true;
int CW =1, CCW=0; 
	
// The serial port:
Serial myPort;
GKnob kb;
GKnob knbStart, knbEnd;
GLabel lblValue, lblAngles;
//GTextArea SID; 
GDropList SID, Baud, Com, SetBaud; 
GCheckbox ComConnect;
GButton ComRefresh; 
GButton TestButton; 
GButton Freset; 
GButton Setbaud; 
GTextField IDtext; 
GLabel IDlabel; 
GButton UnknownID; 
GButton SetID; 
GTextField SetIDtext;
GLabel SetBaudLabel;
GCheckbox WheelMode;
GSlider CW_CCW; 
int dir=1; 
GCustomSlider Speed; 
GLabel SpeedLabel;
GLabel SettingLabel; 
GOption G15Shield; 
GOption G15Driver; 
GToggleGroup tg = new GToggleGroup();
GLabel BoardLabel;
GOption G15LED;

void setup()
{
  // List all the available serial ports:
  println(Serial.list());  
  
  size(500, 360);
  G4P.setGlobalColorScheme(GCScheme.CYAN_SCHEME);
  //G4P.setCursorOff(CROSS);
 // Create the knob using default settings
  kb = new GKnob(this, 10, 95, 180, 180, 0.8f);
  kb.setTurnMode(G4P.CTRL_ANGULAR);
 // kb.setLimits(180, 0, 360);
  kb.setTurnRange(0, 360);
  //kb.setEasing(10);
  kb.setNbrTicks(11);
  kb.setSensitivity(10);  
  kb.setPrecision(1);
  
  int x = (int) (kb.getCX() - 40);
  int y = (int) (kb.getY() -20);
  lblValue = new GLabel(this, x, y, 70, 20);
  lblValue.setTextAlign(GAlign.LEFT,null); 
  lblValue.setOpaque(true);
  lblValue.setText(" Angle: "+ int (kb.getValueF()*360.0));
  
//shows available serial com 
 Com =new GDropList(this, 300, 20, 100, 80); 
 Com.setItems(Serial.list(),0); 
 
//baudrate selector 
 String [] sbaudrate = new String[6]; 
 sbaudrate[0] = ""+baudrates[0]; 
 sbaudrate[1] = ""+baudrates[1]; 
 sbaudrate[2] = ""+baudrates[2];
 sbaudrate[3] = ""+baudrates[3]; 
 sbaudrate[4] = ""+baudrates[4];
 sbaudrate[5] = ""+baudrates[5]; 

 Baud = new GDropList(this, 300, 50, 100, 100 , 6);
 Baud.setItems(sbaudrate,1);
 SetBaudLabel= new GLabel(this, 230, 50, 60, 20, "Baudrate");
 SetBaudLabel.setTextAlign(GAlign.LEFT,null); 
 
 SetBaud = new GDropList(this, 300, 250, 100, 100 , 6);
 SetBaud.setItems(sbaudrate,1);

 
 ComConnect=new GCheckbox(this, 410, 20, 100, 20, "Connect") ;
 ComRefresh= new GButton(this, 230, 20, 60, 20, "Refresh") ;
 //TestButton= new GButton(this, 230, 180, 60, 20, "Ping"); 
 Freset= new GButton(this, 230, 215, 90, 20, "Factory Reset"); 
 Setbaud= new GButton(this, 230, 250, 60, 15, "Set Baud"); 
 
 IDtext= new GTextField(this,300, 80, 100, 20); 
 IDtext.setText("1");
 IDlabel=new GLabel(this, 230, 80, 60, 20, "G15 ID"); 
 IDlabel.setTextAlign(GAlign.LEFT,null); 
 UnknownID=new GButton(this, 405, 80, 80, 20, "Unknown ID?"); 
  
 SetID= new GButton(this, 230, 280, 60, 20, "Set ID");
 SetIDtext= new GTextField(this,300, 280, 100, 20);
 SetIDtext.setText("1");
 
 
 WheelMode=new GCheckbox(this, 230, 120, 100, 20, "Wheel Mode") ;
 CW_CCW= new GSlider (this, 360, 105, 50, 50, 20); 
 String [] ticks= new  String[2]; 
 ticks[0]="CW"; ticks[1]="CCW";
 CW_CCW.setTickLabels(ticks);
 //CW_CCW.setShowDecor(false, true, true, true);
 CW_CCW.setNbrTicks(2);
 CW_CCW.setLimits(1,1,0);
 CW_CCW.setEasing(5); 
 
 int speedslidepos=280;
 Speed= new GCustomSlider(this, 20, speedslidepos, 150, 50, "blue18px"); 
 Speed.setShowDecor(false, true, false, true);
 Speed.setNumberFormat(G4P.INTEGER, 3);
 Speed.setLimits(1023, 0, 1023);
 Speed.setShowValue(true); 
 Speed.setEasing(5);
 Speed.setNbrTicks(3); 
 SpeedLabel=new GLabel(this, 60, speedslidepos+30, 60, 20, "Speed"); 
 SpeedLabel.setTextBold();
 SpeedLabel.setTextItalic();
 
 SettingLabel=new GLabel(this, 230, 185, 80, 20, "Setting G15"); 
 SettingLabel.setTextAlign(GAlign.LEFT,null);
 SettingLabel.setTextBold();
 SettingLabel.setTextItalic();

 G15Driver= new GOption(this, 20,25, 200, 20, "G15 Driver"); 
 G15Shield= new GOption(this, 20,40, 200, 20, "G15 Shield");  
 tg.addControls(G15Shield, G15Driver);
 G15Driver.setSelected(true);
 BoardLabel=new GLabel(this, 20, 5, 80, 20, "Board Select"); 
 BoardLabel.setTextBold();
 BoardLabel.setTextItalic();
 
 G15LED= new GOption(this, 230, 315, 200, 20, "G15 LED"); 
 
}
  
  
void draw()
{
//  char[] data = new char[16];
  background(200, 200, 255);  //set background color

}



public void handleSliderEvents(GValueControl slider, GEvent event)
{
   int val; 
  if(slider==CW_CCW)
  {
    val=CW_CCW.getValueI();
    if(val!=dir)  //debouncing
    {
       dir=val; 
       SetWheelSpeed(1, Speed.getValueI(), dir); 
       println("CW CCW="+ dir ); 
    }
  }
  if(slider==Speed)
  {
     val=Speed.getValueI();
     if(WheelMode.isSelected())
     {
       SetWheelSpeed(1, val, dir); 
     }
     else
     {
       SetSpeed(1,val,iWRITE_DATA);
     }
     println("speed="+val); 	
    
  }  
}

//===========com port refresh============================
public void handleButtonEvents(GButton btn, GEvent event)
{
  char [] data = new char[16];
  char error;  
  if(btn==ComRefresh)
  {
     Com.setItems(Serial.list(),0); 
      if(myPort!=null)
      {
        myPort.stop();  
        myPort=null;    
        
      }      
     
    ComConnect.setSelected(false);  

      println("buttonclick"); 
  }
  if(btn==TestButton)
  {
    error=Ping(1, data);
    println("Ping!"); 
    println(hex(error, 4)); 
    
//    while(true){
//        myPort.setDTR(tx);	
//        myPort.setDTR(rx); 
//    }    
    
  }
  if(btn==Freset)
  {
    FactoryReset(1);
    println("Reset!");
    
  }
  if(btn==Setbaud)
  {
    SetBaudRate(1, baudrates[SetBaud.getSelectedIndex()]);
    
  }
  if(btn==SetID)
  {
    println("setID button");
    int id=Integer.parseInt(SetIDtext.getText()); 
    if(id>253) id=1; 
    SetID(1, id); 
  }
  
  if(btn==UnknownID)
  {
     IDtext.setText("Broadcast");
    
  }
}

//=============com port connect/ disconnect===============================
public void handleToggleControlEvents(GToggleControl connect, GEvent event)
{
  if(connect==ComConnect)
  {
    if(ComConnect.isSelected())
    {
      myPort = new Serial(this, Serial.list()[Com.getSelectedIndex()], baudrates[Baud.getSelectedIndex()]);
      if(myPort==null)
      {
        
      }
    }
    else
    {      
      myPort.stop(); 
      myPort=null;   
    }
  }
  
  if(connect==WheelMode)
  {    
    if(WheelMode.isSelected())
    {
      SetWheelMode(1); 
      SetWheelSpeed(1,Speed.getValueI(),dir); 
      println("wheel Mode"); 
    }
    else
    {
      ExitWheelMode(1);       
    }
  }
  
  if(connect==G15Driver)
  {
    tx =false; 
    rx =true;
    println("board Select=G15 Driver");
  }
  if(connect==G15Shield)
  {
    tx =true; 
    rx =false;    
    println("board Select=G15 Shield");    
  }
  
  if(connect==G15LED)
  {
    if(G15LED.isSelected())
    {
      SetLED(1, 1, iWRITE_DATA);
      println("LED ON");      
    }
    else 
    {
      SetLED(1, 0, iWRITE_DATA);
      println("LED OFF");      
    }    
  }
  
}

//===========com port list, baudrate list, ID list==============
public void handleDropListEvents(GDropList Droplist, GEvent event)
{
  if(Droplist==SID){   
    
  }
  if(Droplist==Com)
  {
    if(myPort!=null)
    {
      myPort.stop(); 
      myPort=null; 
    }     
    ComConnect.setSelected(false);    
  }
  if(Droplist ==Baud)
  {
    // Open the port you are using at the rate you want: 
    myPort.stop();     
    myPort = new Serial(this, Serial.list()[Com.getSelectedIndex()], baudrates[Baud.getSelectedIndex()]); 
  }  
}

public void handleKnobEvents(GValueControl knob, GEvent event)
{ 
  // Has the demo knob turned?
  if (kb == knob && ComConnect.isSelected())  {
    if(WheelMode.isSelected())
    {      
      ExitWheelMode(1); 
      WheelMode.setSelected(false); 
      
    }
    int pos =int (knob.getValueF()*360.0);
    if (pos==360)
    {
      pos=359;
    }
    pos=359-pos;  //change knod direction to be same as G15
    // printf("%d", knob.getValueI());
     lblValue.setText(" Angle: "+ pos);
     
   int langle=pos*1088/360; //get linear angle 
   //SetPos(1, langle, iWRITE_DATA);
   int sp=Speed.getValueI();
   SetPosSpeed(1,langle, sp, iWRITE_DATA);
 
  }  
}
	

