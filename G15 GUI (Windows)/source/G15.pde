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
  



char send_packet(int ID, char inst, char data[], char param_len)	
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
    TxBuff[2] = char(ID); 		          checksum += TxBuff[2];	
    TxBuff[3] = char(param_len + 2);          checksum += TxBuff[3];	                                                                                                   
    TxBuff[4] = inst;		          checksum += TxBuff[4];	

    for(i = 0; i < param_len; i++)		//data
    {
      TxBuff[i+5] = data[i];
      checksum += TxBuff[i+5];
    }
    TxBuff[i+5] =char(~checksum); 				//Checksum with Bit Inversion

    packet_len =char(TxBuff[3] + 4);			//# of bytes for the whole packet
    
    for(i=0; i<packet_len;i++)
    {
      myPort.write(TxBuff[i]);                          //the library waits for transfer complete, output.flush() is called
    }
 
    delay(2);                                           //for 9600 baud, not finish sending last byte
    myPort.setDTR(rx);    		                //set to receive mode and start receiving from G15
	


    // we'll only get a reply if it was not broadcast
    if((ID != int(0xFE)) || (inst == iPING))
    {
        if(inst == iREAD_DATA)			        //if a read instruction
	{
	  param_len = data[1]; 
          packet_len = char(data[1] + 6);  	        // data[1] = length of the data to be read
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

        char readcount= char(myPort.readBytes(Status) );        //read the data out of buffer	


 //       myPort.setDTR(tx);       //set to transmit mode
        
        println("bytes="+int(readcount));
        for(int k=0; k<readcount; k++)
        {
          println(hex(Status[k]));
        }
        
        if(readcount==(packet_len-char(2)))
          println("headerprob"); 
			
	//Checking received bytes
	error=0; 		//clear error 
	if(readcount!=packet_len)
        {
          error|=0x0100; 
	  //return (error);			        //packet lost or receive time out 
	}

	if ((char(Status[0]) !=char(0xFF)) || (char(Status[1]) != char(0xFF)))	
	{
	  error|=0x0200; 
	  //return (error);			        //1000 00001	//wrong header
	}
	if (Status[2] != char(ID))
	{
	  error|=0x0400;
	  //return (error);			//ID mismatch
	}
	if(Status[4] != char(0))		
	{
	  error|=Status[4]; 
	  //return(error); 
	}
	// calculate checksum
	checksum = 0;					//clear checksum value
        for(i = 2; i < packet_len; i++)	//whole package including checksum but excluding header
        {
          checksum += char(Status[i]);		//correct end result must be 0xFF
        }
        if(checksum != char(0xFF))
        {
          error |= 0x0800;       		//return packet checksum mismatch error
          //return (error);
        }
        if(Status[4]==char(0x00) && (error&0x0100)==0x00)	//copy data only if there is no packet error
	{       
	  if(inst == iPING)
	  {
	    // ID is passed to the data[0]
	    data[0] = char (Status[2]);
	   } 
	  else if(inst == iREAD_DATA)
	  {
	    for(i = 0; i < param_len; i++)  //Requested Parameters
		data[i] = char (Status[i+5]);
	  }
	}
		
    }
//    myPort.setDTR(tx);       //set to transmit mode

    return(error); // return error code	 	
}


char SetPos(int ServoID, int Position, char Write_Reg)
{
    char[] TxBuff= new char[3];	
    char pos= char(Position);
	
    TxBuff[0] = GOAL_POSITION_L;	//Control Starting Address
    TxBuff[1] = char(pos & 0x00FF);  			//goal pos bottom 8 bits
    TxBuff[2] = char(pos >>8); 			//goal pos top 8 bits
	
	// write the packet, return the error code
  return(send_packet(ServoID, Write_Reg, TxBuff, char(3)));
}
char SetPosSpeed(int ServoID, int Position, int Speed, char Write_Reg)
{
    char[] TxBuff= new char[5];	
    char pos= char(Position);
	
    TxBuff[0] = GOAL_POSITION_L;	//Control Starting Address
    TxBuff[1] = char(pos & 0x00FF);  			//goal pos bottom 8 bits
    TxBuff[2] = char(pos >>8); 			//goal pos top 8 bits
    TxBuff[3] = char(Speed & 0x00FF); 
    TxBuff[4] = char(Speed >>8); 
	
	// write the packet, return the error code
  return(send_packet(ServoID, Write_Reg, TxBuff, char(5)));
}
char Ping(int ServoID, char data[])
{
	// write the packet, return the error code
    return(send_packet(ServoID, iPING, data, char(0)));
}


char FactoryReset(int ServoID)
{
	char[] TxBuff= new char[1];		//dummy byte
	char error; 
	
	error=send_packet(ServoID, iRESET, TxBuff, char(0)); 
	delay(100); 		//delay for eeprom write

    // write the packet, return the error code
    return(error);
}
char SetBaudRate(int ServoID, int bps)
{
	char[] TxBuff= new char[2];
	char error; 
	

        TxBuff[0] = BAUD_RATE;			//Control Starting Address
	TxBuff[1] = char ((2000000/bps)-1);	//Calculate baudrate
                                                //Speed (BPS) = 32M / (16*(n + 1))=2000000/(n+1)
        println( ""+ int(TxBuff[1]));                                               
									
	error=send_packet(ServoID, iWRITE_DATA, TxBuff, char(2)); 
	delay(10); 

    // write the packet, return the error code
    return(error);
}

char SetID(int ServoID, int NewID)
{
    char[] TxBuff= new char[2];
    char error;  
	
    TxBuff[0] = ID;			//Control Starting Address
    TxBuff[1] = char(NewID);	
	
    error=send_packet(ServoID, iWRITE_DATA, TxBuff, char(2));
 
    delay(10); 			//delay for eeprom write
    return(error);
	
}

char SetWheelSpeed(int ServoID, int Speed, int CW_CCW)
{
  Speed=Speed&0x03FF; 	//0000 0011 1111 1111 eliminate bits which are non speed
  if(CW_CCW==1){		//if CW
    Speed=Speed|0x0400; 
  }
  return(SetSpeed(ServoID, Speed,iWRITE_DATA));	
}

char SetSpeed(int ServoID, int Speed, char Write_Reg)
{
    char[] TxBuff= new char[3];

    TxBuff[0] = MOVING_SPEED_L;		//Control Starting Address
    TxBuff[1] = char(Speed&0x00FF);			//speed bottom 8 bits
    TxBuff[2] = char(Speed>>8); 			//speed top 8 bits
	
    // write the packet, return the error code
    return(send_packet(ServoID, Write_Reg, TxBuff, char(3)));
} 

char SetWheelMode(int ServoID)	//10 bits speed (0-1024)
{		
	char Error=0; 
	Error=SetAngleLimit(ServoID, 0,0);	//enable wheel mode
	//if(Error!=0) return (Error);
	
	Error = SetTorqueOnOff(ServoID, 1, iWRITE_DATA);	//enable torque		
	
	return(Error);    
}
char ExitWheelMode(int ServoID)
{
	return(SetAngleLimit(ServoID, 0,1087));  //reset to default angle limit
}

char SetAngleLimit(int ServoID, int CW_angle, int CCW_angle)
{
  char[] TxBuff= new char[5];
  char error; 

  TxBuff[0] = CW_ANGLE_LIMIT_L;			//Starting Address
  TxBuff[1] = char(CW_angle&0x00FF);  	//CW limit bottom 8 bits
  TxBuff[2] = char(CW_angle>>8); 			//CW limit top 8 bits
  TxBuff[3] = char(CCW_angle&0x00FF); 	//CCW limit bottom 8 bits
  TxBuff[4] = char(CCW_angle>>8); 		//CCW limit top 8 bits

	
  error = send_packet(ServoID, iWRITE_DATA, TxBuff, char(5)); 
  delay(10); 		//delay for eeprom write
    // write the packet, return the error code
  return(error);
}

char SetTorqueOnOff(int ServoID,int on_off, char Write_Reg)
{
  char[] TxBuff= new char[2];

  TxBuff[0] = TORQUE_ENABLE;		//Control Starting Address
  TxBuff[1] = char(on_off); 			//ON = 1, OFF = 0

    // write the packet, return the error code
    return(send_packet(ServoID, Write_Reg, TxBuff, char(2)));
}

char SetLED(int ServoID, int on_off, char Write_Reg)
{
    char[] TxBuff= new char[2];

    TxBuff[0] = LED;				//Control Starting Address
    TxBuff[1] = char(on_off); 			//ON = 1, OFF = 0

    // write the packet, return the error code
    return(send_packet(ServoID, Write_Reg, TxBuff, char(2)));
	
}  
