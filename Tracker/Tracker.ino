/*****************************************************************************
The Geogram ONE is an open source tracking device/development board based off 
the Arduino platform.  The hardware design and software files are released 
under CC-SA v3 license.
*****************************************************************************/

#include <AltSoftSerial.h>
#include <PinChangeInt.h>
#include "GeogramONE.h"
#include <EEPROM.h>
#include <I2C.h>
#include "eepromAnything.h"

#define USEFENCE1			1  //set to zero to free up code space if option is not needed
#define USESPEED			1  //set to zero to free up code space if option is not needed
#define USEMOTION			1  //set to zero to free up code space if option is not needed
#define USESECURITY			1  //set to zero to free up code space if option is not needed
#define USETRACKING                     1
#define USECALL                         1  //set to zero to disable sms commands
#define USEUDP                          0

GeogramONE ggo;
AltSoftSerial GSM;
SIM900 sim900(&GSM);
geoSmsData smsData;
PA6C gps(&Serial); 
goCoord lastValid;
geoFence fence;

volatile uint8_t call;
volatile uint8_t move;
volatile boolean stolen=0;
volatile uint8_t battery = 0;
//volatile uint8_t charge = 0x02; // force a read of the charger cable
volatile uint8_t charge = 0x00; //assume its not charging
volatile uint8_t d4Switch = 0x00;
volatile uint8_t d10Switch = 0x00;

uint8_t cmd0 = 0;
uint8_t cmd1 = 0;
uint8_t cmd3 = 0;
uint8_t udp = 0x00; 

//#if USEFENCE1
uint8_t fence1 = 0;
uint8_t breach1Conf = 0;

uint8_t breachSpeed = 0;
uint8_t breachReps = 0;

uint32_t smsInterval = 0;
uint32_t udpInterval = 0;
uint32_t sleepTimeOn = 0;
uint32_t sleepTimeOff = 0;
uint8_t sleepTimeConfig = 0;

uint8_t speedHyst = 0;
uint16_t speedLimit = 0;

char udpReply[11];
uint8_t smsPowerProfile = 0;
uint8_t udpPowerProfile = 0;
uint8_t smsPowerSpeed = 0;
uint8_t udpPowerSpeed = 0;

bool gsmPowerStatus = true;

void goesWhere(char *, uint8_t replyOrStored = 0);
bool engMetric;

unsigned long armWindow = 60000; // Arms in 60 sec after engine turnoff - You need to hop off the bike within this window
unsigned long disarmWindow = 20000; // Disarms after getting on bike and turning on engine within 20 sec, otherwise it will start alerting
unsigned long disarmTimer;
unsigned long armTimer;
boolean moveFlag = false;
boolean armFlag = false;
boolean trackFlag = false;
int moveCount = 0;

void setup()
{
        Serial.begin(115200);
        Serial.println("Starting...");
	ggo.init();
	gps.init(115200);
	sim900.init(9600);
	MAX17043init(7, 500);
	BMA250init(3, 500);
	attachInterrupt(0, ringIndicator, FALLING);
	attachInterrupt(1, movement, FALLING);
	PCintPort::attachInterrupt(PG_INT, &charger, CHANGE);
	PCintPort::attachInterrupt(FUELGAUGEPIN, &lowBattery, FALLING);
	goesWhere(smsData.smsNumber);
	call = sim900.checkForMessages();
	if(call == 0xFF)
		call = 0;
	battery = MAX17043getAlertFlag();
	#if USESPEED
	ggo.configureSpeed(&cmd3, &speedHyst, &speedLimit);
	#endif

        #if USESECURITY
        Serial.println("Starting HTTP...");
        setupHTTP();
        Serial.println("HTTP Started.");
        #endif // USESECURITY

	ggo.configureBreachParameters(&breachSpeed, &breachReps);
	ggo.configureSleepTime(&sleepTimeOn, &sleepTimeOff, &sleepTimeConfig);
	BMA250enableInterrupts();
	uint8_t swInt = EEPROM.read(IOSTATE0);
	if(swInt == 0x05)
		PCintPort::attachInterrupt(4, &d4Interrupt, RISING);
	if(swInt == 0x06)
		PCintPort::attachInterrupt(4, &d4Interrupt, FALLING);
	swInt = EEPROM.read(IOSTATE1);
	if(swInt == 0x05)
		PCintPort::attachInterrupt(10, &d10Interrupt, RISING);
	if(swInt == 0x06)
		PCintPort::attachInterrupt(10, &d10Interrupt, FALLING);
        Serial.println("Setup Finished.");
}

void loop()
{
	if(!gps.getCoordinates(&lastValid))
	{
		int8_t tZ = EEPROM.read(TIMEZONE);
		bool eM = EEPROM.read(ENGMETRIC);
		gps.updateRegionalSettings(tZ, eM, &lastValid);
	}
        #if USECALL
        call = sim900.checkForMessages();
	if(call)
	{
		sim900.gsmSleepMode(0);
		char pwd[5];
		EEPROM_readAnything(PINCODE,pwd);
		if(sim900.signalQuality())
		{
                        Serial.println(sim900.signalQuality());
			if(!sim900.getGeo(&smsData, pwd))
			{
				if(!smsData.smsPending)
					call = 0; // no more messages
				if(smsData.smsDataValid)
				{
					if(!smsData.smsCmdNum)
						cmd0 = 0x01;
					else if(smsData.smsCmdNum == 1){
                                                Serial.println("C1CommandRecieved");
						cmd1 = 0x01;}
                                        else if(smsData.smsCmdNum == 9){
                                            Serial.println("TrackCommandRecieved");
					    httpPost(2);
                                            //trackFlag = 1;
                                        }
                                        else if(smsData.smsCmdNum == 10){
					    trackFlag = 0;
                                            disarm();
                                        }
					else if(smsData.smsCmdNum == 255)
					{
						sim900.gsmSleepMode(0);
						sim900.powerDownGSM();
						delay(2000);
						sim900.init(9600);
						gsmPowerStatus = true;
					}
				}
			}
		}
		sim900.gsmSleepMode(2);	
	}
        #endif
	if(cmd0)
		command0();
        
	#if USESECURITY
        // Bike Off - Armed
        if(charge == 0){
          // Record parking spot - 0
          if(!armFlag){
            armFlag = true;
            httpPost(0);
          }
          if(stolen==1)
              {delay(5000);Serial.println("Stolen");httpPost(3);}
          if(move && stolen==0){ 
              if((millis() - armTimer) > armWindow ){
                 if(!moveFlag){
                   disarmTimer = millis();
                   moveFlag = true;
                 }
                 if(millis() - disarmTimer > disarmWindow){
                   if(fence1){
                     httpPost(2); stolen=1;
                   }
                    else
                      httpPost(1);
                 }
              }
              else{
                move = 0;
              }
           }
        }
        // Bike On - Disarmed
        else{
          armTimer = millis();
          move = 0;
          moveFlag = false;
          armFlag = false;
        }
        
        #endif // USESECURITY
        

	if(charge & 0x02)
		chargerStatus();

	engMetric = EEPROM.read(ENGMETRIC);

	#if USEFENCE1
        if(fence1 == 0 && !gps.getCoordinates(&lastValid)){
          setGeofence();
	  ggo.getFenceActive(1, &fence1);
        }
        
	if((fence1 == 1))
	{
		ggo.configureFence(1,&fence); 
		if(!gps.geoFenceDistance(&lastValid, &fence, engMetric))
		{
			if(lastValid.updated & 0x02)
				breach1Conf++;
			if(breach1Conf > breachReps)
			{
				fence1 = 2;
				breach1Conf = 0;
			}
			lastValid.updated &= ~(0x02); 
		}
		else
			breach1Conf = 0;
	}
	else
		breach1Conf = 0;

	
	#endif
/*
	if(smsInterval)
		smsTimerMenu();
	if(udpInterval)
		udpTimerMenu();
	if(sleepTimeOn && sleepTimeOff)
		sleepTimer();
*/
	if(gsmPowerStatus)
		sim900.initializeGSM();
} 

void setGeofence(){
  Serial.println("Setting geofence...");
  uint8_t cmd;
  uint8_t offset = 0;
  unsigned long cmdLong;
  long latLonSigned;
  cmd = 1 & 0x01; // deactivate 0 or activate 1
  EEPROM.write(ACTIVE1 + offset,cmd);
  cmd = 0 & 0x01; // inside fence 0 or outside fence 1
  EEPROM.write(INOUT1 + offset,cmd);
  cmdLong = (long) 100; // fence radius
  EEPROM_writeAnything(RADIUS1 + offset, cmdLong);
  latLonSigned = (long)(atof(lastValid.latitude) *10000);
  if(lastValid.ns == 'S')
	latLonSigned *= -1;
  EEPROM_writeAnything(LATITUDE1 + offset, latLonSigned);
  latLonSigned = (long)(atof(lastValid.longitude) *10000);
    if(lastValid.ew == 'W')
      latLonSigned *= -1;
   EEPROM_writeAnything(LONGITUDE1 + offset, latLonSigned);  
   Serial.println("Geofence set.");		
}

void printEEPROM(uint16_t eAddress)
{
	char eepChar;
	for (uint8_t ep = 0; ep < 50; ep++)
	{
		eepChar = EEPROM.read(ep + eAddress);
		if(eepChar == '\0')
			break;
		else
			GSM.print(eepChar);
	}
}

void goesWhere(char *smsAddress, uint8_t replyOrStored)
{
	if(!replyOrStored)
		EEPROM_readAnything(RETURNADDCONFIG,replyOrStored);
	if((replyOrStored == 2) || ((replyOrStored == 3) && (smsAddress[0] == NULL)))
	for(uint8_t l = 0; l < 39; l++)
	{
			smsAddress[l] = EEPROM.read(l + SMSADDRESS);
			if(smsAddress[l] == NULL)
				break;
	}
}

void disarm(){
    armTimer = millis();
    move = 0;
    moveFlag = false;
    armFlag = false;
}

