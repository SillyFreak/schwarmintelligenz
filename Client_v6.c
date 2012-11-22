//#########################################################################
//Client_mitThreads.c
//last edited by Daniel F. 22.Nov
//works, but "create connect failed" sometimes (why?)
//#########################################################################

#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <pthread.h>


//connection
#define	SERVER_PORT		28109
#define	SERVER_IP		"192.168.1.10"

#define PINKIP			"192.168.1.11"
#define ORANGEIP		"192.168.1.12"
#define GREENIP			"192.168.1.13"
#define BLUEIP			"192.168.1.14"

//modi commands
#define PROGRAMM_PINK		"fo00"
#define PROGRAMM_ORANGE		"fo01"
#define PROGRAMM_GREEN		"fo02"
#define PROGRAMM_BLUE		"fo03"
#define PROGRAMM_FRONT		"fo04"

//status
#define ACTIVE			"actv\n"
#define INACTIVE		"iact\n"
#define AREYOUACTIVE		"stat"

#define MIN_CBC_VOLTAGE		6.7
#define CBC_FULL_VOLTAGE    	8.2
#define MIN_CREATE_CHARGE	500

//channels/missions
#define PINK			0
#define ORANGE			1
#define GREEN			2
#define BLUE			3
#define FRONT			4
#define CHARGE			5
#define IDLE			6




int mission=IDLE;
int client_socket;

#include "Tracking_v6.c"


//####################################################################
//sets up network connection
//####################################################################

void networkSetup()
{
	//jumper digital 8-11 for colors pink-blue
	char myip[16];
	if(digital(8)) strcpy(myip,PINKIP);
	else if(digital(9)) strcpy(myip,ORANGEIP);
	else if(digital(10)) strcpy(myip,GREENIP);
	else if(digital(11)) strcpy(myip,BLUEIP);
	else {printf("not jumpered correctly!\nexiting program!\n"); exit(0);}
	
	char string[50]="ifconfig rausb0 ";
	strcat(string,myip);

	system("iwconfig rausb0 essid BotGuy");
	system("ifconfig rausb0 key open");
	system("dhclient rausb0");
	system(string);
	system("ifconfig");
	cbc_display_clear();

	printf("connected to network\n");
}
	
	
//####################################################################
//listens for server commands and reacts to them
//####################################################################

void serverCommunication()
{
	int strlenght;
	char msg[5];

	while (1)
	{
		strlenght=recv(client_socket,msg,5,0);
		msg[strlenght]='\0';
		printf("Received message: %s\n",msg);
        
        if (strcmp(msg,AREYOUACTIVE)) 
		{       
            if (strcmp(msg,"")==0)
			{
				close(client_socket);
				printf("server fail!\n");
				exit(1);
			}
          
            if (strcmp(msg,PROGRAMM_PINK)==0)	{printf("I will follow PINK!\n");	mission=PINK;}
            if (strcmp(msg,PROGRAMM_ORANGE)==0)	{printf("I will follow ORANGE!\n");	mission=ORANGE;}
            if (strcmp(msg,PROGRAMM_GREEN)==0)	{printf("I will follow GREEN!\n");	mission=GREEN;}
            if (strcmp(msg,PROGRAMM_BLUE)==0)	{printf("I will follow BLUE!\n");	mission=BLUE;}
            if (strcmp(msg,PROGRAMM_FRONT)==0)	{printf("I will drive in front\n");	mission=FRONT;}
            		
        }
		else	//occurs regularly
		{
            		
			if ((power_level()<MIN_CBC_VOLTAGE)||(create_get_charge()<MIN_CREATE_CHARGE))
			{
				send(client_socket,INACTIVE,sizeof(INACTIVE),0);
				mission=CHARGE;
			}
			else
			{
				send(client_socket,ACTIVE,sizeof(ACTIVE),0);
			}	
		}
	}
}


//####################################################################
//does the actual control of the robot
//####################################################################

void *control(int *unused)
{
	while(1)
	{
		if(mission>6)
		{
			printf("invalid value in mission: %d\n",mission);
		}
		else
		{
			switch(mission)
			{
				case FRONT: drive_random(); break;
				case CHARGE: charge(); break;
				case IDLE: create_stop(); break;
				default: track(mission);
			}
		}
	}
}


//####################################################################
//main
//####################################################################

void main()
{
	networkSetup();
	if (create_connect())exit(0);
	sleep(1);
	create_full();
	connectToServer();

	//control() in new thread
	int unused = 0;
	pthread_t controlThread;
	int threadError = pthread_create (&controlThread,NULL,control, &unused);
	if(threadError)
	{
		printf("thread could not be started! \nerror: %d \nexiting program!\n",threadError);
		exit(0);
	}

	serverCommunication();
}
