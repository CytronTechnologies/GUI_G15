import processing.core.*; 
import processing.xml.*; 

import processing.serial.*; 
import g4p_controls.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class G15GUI extends PApplet {




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

public void setup()
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
  lblValue.setText(" Angle: "+ PApplet.parseInt (kb.getValueF()*360.0f));
  
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
  
  
public void draw()
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
    int pos =PApplet.parseInt (knob.getValueF()*360.0f);
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
	

class G15{  
  char ID; 
  G15(char id){
    ID=id;
  }
}


//******************************************************************
//*	INSTRUCTIONS
//******************************************************************
char iPING        =0x01; //obtain a status packet
char iREAD_DATA	  =0x02; //read Control Table values
char iWRITE_DATA  =0x03; //write Control Table values
char iREG_WRITE   =0x04; //write and wait for ACTION instruction
char iACTION 	  =0x05; //triggers REG_WRITE instruction
char iRESET       =0x06; //set factory defaults
char iSYNC_WRITE  =0x83; //simultaneously control multiple actuators

//#define ConvertAngle2Pos(Angle) word(word(Angle)*1088UL/360UL)
//#define ConvertPos2Angle(Pos) float(Pos)*360.0/1088.0
//#define ConvertTime(Time) word(Time*10UL)
//#define CW 1
//#define CCW 0
//#define ON 1
//#define OFF 0
////Alarm Mask	1111 1111
//#define ALARM_INST			0x40		
//#define ALARM_OVERLOAD		0x20
//#define ALARM_CHECKSUM		0x10
//#define ALARM_RANGE			0x08
//#define ALARM_OVERHEAT		0x04
//#define ALARM_ANGLELIMIT 	0x02
//#define ALARM_VOLTAGE		0x01

char  MODEL_NUMBER_L=0x00;
char  MODEL_NUMBER_H=0x01;
char  VERSION=0x02;
char  ID=0x03;
char  BAUD_RATE=0x04;
char  RETURN_DELAY_TIME=0x05;
char  CW_ANGLE_LIMIT_L=0x06;
char  CW_ANGLE_LIMIT_H=0x07;
char  CCW_ANGLE_LIMIT_L=0x08;
char  CCW_ANGLE_LIMIT_H=0x09;
char  RESERVED1=0x0A;
char  LIMIT_TEMPERATURE=0x0B;
char  DOWN_LIMIT_VOLTAGE=0x0C;
char  UP_LIMIT_VOLTAGE=0x0D;
char  MAX_TORQUE_L=0x0E;
char  MAX_TORQUE_H=0x0F;
char  STATUS_RETURN_LEVEL=0x10;	
char  ALARM_LED=0x11;
char  ALARM_SHUTDOWN=0x12;
char  RESERVED2=0x13;
char  DOWN_CALIBRATION_L=0x14;
char  DOWN_CALIBRATION_H=0x15;
char  UP_CALIBRATION_L=0x16;
char  UP_CALIBRATION_H=0x17;
char  TORQUE_ENABLE=0x18;
char  LED=0x19;
char  CW_COMPLIANCE_MARGIN=0x1A;
char  CCW_COMPLIANCE_MARGIN=0x1B;
char  CW_COMPLIANCE_SLOPE=0x1C;
char  CCW_COMPLIANCE_SLOPE=0x1D;
char  GOAL_POSITION_L=0x1E;
char  GOAL_POSITION_H=0x1F; 
char  MOVING_SPEED_L=0x20; 
char  MOVING_SPEED_H=0x21; 
char  TORQUE_LIMIT_L=0x22; 
char  TORQUE_LIMIT_H=0x23; 
char  PRESENT_POSITION_L=0x24; 
char  PRESENT_POSITION_H=0x25; 
char  PRESENT_SPEED_L=0x26; 
char  PRESENT_SPEED_H=0x27; 
char  PRESENT_LOAD_L=0x28; 
char  PRESENT_LOAD_H=0x29; 
char  PRESENT_VOLTAGE=0x2A; 
char  PRESENT_TEMPERATURE=0x2B; 
char  REGISTERED_INSTRUCTION=0x2C; 
char  RESERVE3=0x2D; 
char  MOVING=0x2E; 
char  LOCK=0x2F; 
char  PUNCH_L=0x30;
char  PUNCH_H=0x31;
  



public char send_packet(int ID, char inst, char data[], char param_len)	
{
    int i; 
    char packet_len  = 0;
    char[] TxBuff = new char[16];
    String id; 
    
    id=IDtext.getText();
    if(id.equals("Broadcast"))
    {
      ID=254; 
    }
    else
    {    
      ID=Integer.parseInt(id); 
    }
    println(""+ID); 
    
    char checksum=0; 		//Check Sum = ~ (ID + Length + Instruction + Parameter1 + ... Parameter N)
    char error=0; 
	
    myPort.setDTR(tx);					//set for transmit mode
    myPort.clear();    //clear all buffers
	
    checksum=0;					//clear checksum value
    TxBuff[0] = 0xFF;			//0xFF not included in checksum
    TxBuff[1] = 0xFF;
    TxBuff[2] = PApplet.parseChar(ID); 		          checksum += TxBuff[2];	
    TxBuff[3] = PApplet.parseChar(param_len + 2);          checksum += TxBuff[3];	                                                                                                   
    TxBuff[4] = inst;		          checksum += TxBuff[4];	

    for(i = 0; i < param_len; i++)		//data
    {
      TxBuff[i+5] = data[i];
      checksum += TxBuff[i+5];
    }
    TxBuff[i+5] =PApplet.parseChar(~checksum); 				//Checksum with Bit Inversion

    packet_len =PApplet.parseChar(TxBuff[3] + 4);			//# of bytes for the whole packet
    
    for(i=0; i<packet_len;i++)
    {
      myPort.write(TxBuff[i]);                          //the library waits for transfer complete, output.flush() is called
    }
 
    delay(2);                                           //for 9600 baud, not finish sending last byte
    myPort.setDTR(rx);    		                //set to receive mode and start receiving from G15
	


    // we'll only get a reply if it was not broadcast
    if((ID != PApplet.parseInt(0xFE)) || (inst == iPING))
    {
        if(inst == iREAD_DATA)			        //if a read instruction
	{
	  param_len = data[1]; 
          packet_len = PApplet.parseChar(data[1] + 6);  	        // data[1] = length of the data to be read
	}
        else
	{
          packet_len = 6;
	}	
        
        byte [] Status = new byte [packet_len];
        
      
        int count=SerialTimeOut*packet_len*100;
         
         while (myPort.available() <packet_len) 
        {
          count--; 
          if(count==0) break; 
        }

        char readcount= PApplet.parseChar(myPort.readBytes(Status) );        //read the data out of buffer	


 //       myPort.setDTR(tx);       //set to transmit mode
        
        println("bytes="+PApplet.parseInt(readcount));
        for(int k=0; k<readcount; k++)
        {
          println(hex(Status[k]));
        }
        
        if(readcount==(packet_len-PApplet.parseChar(2)))
          println("headerprob"); 
			
	//Checking received bytes
	error=0; 		//clear error 
	if(readcount!=packet_len)
        {
          error|=0x0100; 
	  //return (error);			        //packet lost or receive time out 
	}

	if ((PApplet.parseChar(Status[0]) !=PApplet.parseChar(0xFF)) || (PApplet.parseChar(Status[1]) != PApplet.parseChar(0xFF)))	
	{
	  error|=0x0200; 
	  //return (error);			        //1000 00001	//wrong header
	}
	if (Status[2] != PApplet.parseChar(ID))
	{
	  error|=0x0400;
	  //return (error);			//ID mismatch
	}
	if(Status[4] != PApplet.parseChar(0))		
	{
	  error|=Status[4]; 
	  //return(error); 
	}
	// calculate checksum
	checksum = 0;					//clear checksum value
        for(i = 2; i < packet_len; i++)	//whole package including checksum but excluding header
        {
          checksum += PApplet.parseChar(Status[i]);		//correct end result must be 0xFF
        }
        if(checksum != PApplet.parseChar(0xFF))
        {
          error |= 0x0800;       		//return packet checksum mismatch error
          //return (error);
        }
        if(Status[4]==PApplet.parseChar(0x00) && (error&0x0100)==0x00)	//copy data only if there is no packet error
	{       
	  if(inst == iPING)
	  {
	    // ID is passed to the data[0]
	    data[0] = PApplet.parseChar (Status[2]);
	   } 
	  else if(inst == iREAD_DATA)
	  {
	    for(i = 0; i < param_len; i++)  //Requested Parameters
		data[i] = PApplet.parseChar (Status[i+5]);
	  }
	}
		
    }
//    myPort.setDTR(tx);       //set to transmit mode

    return(error); // return error code	 	
}


public char SetPos(int ServoID, int Position, char Write_Reg)
{
    char[] TxBuff= new char[3];	
    char pos= PApplet.parseChar(Position);
	
    TxBuff[0] = GOAL_POSITION_L;	//Control Starting Address
    TxBuff[1] = PApplet.parseChar(pos & 0x00FF);  			//goal pos bottom 8 bits
    TxBuff[2] = PApplet.parseChar(pos >>8); 			//goal pos top 8 bits
	
	// write the packet, return the error code
  return(send_packet(ServoID, Write_Reg, TxBuff, PApplet.parseChar(3)));
}
public char SetPosSpeed(int ServoID, int Position, int Speed, char Write_Reg)
{
    char[] TxBuff= new char[5];	
    char pos= PApplet.parseChar(Position);
	
    TxBuff[0] = GOAL_POSITION_L;	//Control Starting Address
    TxBuff[1] = PApplet.parseChar(pos & 0x00FF);  			//goal pos bottom 8 bits
    TxBuff[2] = PApplet.parseChar(pos >>8); 			//goal pos top 8 bits
    TxBuff[3] = PApplet.parseChar(Speed & 0x00FF); 
    TxBuff[4] = PApplet.parseChar(Speed >>8); 
	
	// write the packet, return the error code
  return(send_packet(ServoID, Write_Reg, TxBuff, PApplet.parseChar(5)));
}
public char Ping(int ServoID, char data[])
{
	// write the packet, return the error code
    return(send_packet(ServoID, iPING, data, PApplet.parseChar(0)));
}


public char FactoryReset(int ServoID)
{
	char[] TxBuff= new char[1];		//dummy byte
	char error; 
	
	error=send_packet(ServoID, iRESET, TxBuff, PApplet.parseChar(0)); 
	delay(100); 		//delay for eeprom write

    // write the packet, return the error code
    return(error);
}
public char SetBaudRate(int ServoID, int bps)
{
	char[] TxBuff= new char[2];
	char error; 
	

        TxBuff[0] = BAUD_RATE;			//Control Starting Address
	TxBuff[1] = PApplet.parseChar ((2000000/bps)-1);	//Calculate baudrate
                                                //Speed (BPS) = 32M / (16*(n + 1))=2000000/(n+1)
        println( ""+ PApplet.parseInt(TxBuff[1]));                                               
									
	error=send_packet(ServoID, iWRITE_DATA, TxBuff, PApplet.parseChar(2)); 
	delay(10); 

    // write the packet, return the error code
    return(error);
}

public char SetID(int ServoID, int NewID)
{
    char[] TxBuff= new char[2];
    char error;  
	
    TxBuff[0] = ID;			//Control Starting Address
    TxBuff[1] = PApplet.parseChar(NewID);	
	
    error=send_packet(ServoID, iWRITE_DATA, TxBuff, PApplet.parseChar(2));
 
    delay(10); 			//delay for eeprom write
    return(error);
	
}

public char SetWheelSpeed(int ServoID, int Speed, int CW_CCW)
{
  Speed=Speed&0x03FF; 	//0000 0011 1111 1111 eliminate bits which are non speed
  if(CW_CCW==1){		//if CW
    Speed=Speed|0x0400; 
  }
  return(SetSpeed(ServoID, Speed,iWRITE_DATA));	
}

public char SetSpeed(int ServoID, int Speed, char Write_Reg)
{
    char[] TxBuff= new char[3];

    TxBuff[0] = MOVING_SPEED_L;		//Control Starting Address
    TxBuff[1] = PApplet.parseChar(Speed&0x00FF);			//speed bottom 8 bits
    TxBuff[2] = PApplet.parseChar(Speed>>8); 			//speed top 8 bits
	
    // write the packet, return the error code
    return(send_packet(ServoID, Write_Reg, TxBuff, PApplet.parseChar(3)));
} 

public char SetWheelMode(int ServoID)	//10 bits speed (0-1024)
{		
	char Error=0; 
	Error=SetAngleLimit(ServoID, 0,0);	//enable wheel mode
	//if(Error!=0) return (Error);
	
	Error = SetTorqueOnOff(ServoID, 1, iWRITE_DATA);	//enable torque		
	
	return(Error);    
}
public char ExitWheelMode(int ServoID)
{
	return(SetAngleLimit(ServoID, 0,1087));  //reset to default angle limit
}

public char SetAngleLimit(int ServoID, int CW_angle, int CCW_angle)
{
  char[] TxBuff= new char[5];
  char error; 

  TxBuff[0] = CW_ANGLE_LIMIT_L;			//Starting Address
  TxBuff[1] = PApplet.parseChar(CW_angle&0x00FF);  	//CW limit bottom 8 bits
  TxBuff[2] = PApplet.parseChar(CW_angle>>8); 			//CW limit top 8 bits
  TxBuff[3] = PApplet.parseChar(CCW_angle&0x00FF); 	//CCW limit bottom 8 bits
  TxBuff[4] = PApplet.parseChar(CCW_angle>>8); 		//CCW limit top 8 bits

	
  error = send_packet(ServoID, iWRITE_DATA, TxBuff, PApplet.parseChar(5)); 
  delay(10); 		//delay for eeprom write
    // write the packet, return the error code
  return(error);
}

public char SetTorqueOnOff(int ServoID,int on_off, char Write_Reg)
{
  char[] TxBuff= new char[2];

  TxBuff[0] = TORQUE_ENABLE;		//Control Starting Address
  TxBuff[1] = PApplet.parseChar(on_off); 			//ON = 1, OFF = 0

    // write the packet, return the error code
    return(send_packet(ServoID, Write_Reg, TxBuff, PApplet.parseChar(2)));
}

public char SetLED(int ServoID, int on_off, char Write_Reg)
{
    char[] TxBuff= new char[2];

    TxBuff[0] = LED;				//Control Starting Address
    TxBuff[1] = PApplet.parseChar(on_off); 			//ON = 1, OFF = 0

    // write the packet, return the error code
    return(send_packet(ServoID, Write_Reg, TxBuff, PApplet.parseChar(2)));
	
}  
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "G15GUI" });
  }
}
